#!/usr/bin/env python3
"""Reproduce isolated Android acceptance checks; never signs or publishes.

Default scaffolds only. --execute explicitly permits official wrapper downloads
and build execution. --extend-existing is restricted to a suite-generated demo
with the marker produced by this script. It never operates on arbitrary apps.
"""
import argparse
from pathlib import Path
import shutil
import sys

from suite import (PLUGIN, SuiteError, bootstrap_wrapper, generate_api, init_project,
                   new_feature, verify, write_json)

API_TEST = '''package com.example.suitedemo.data

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogContractTest {
    private fun withApi(block: suspend (ApiRepository, MockWebServer) -> Unit) = runTest {
        val certificate = HeldCertificate.Builder()
            .addSubjectAlternativeName("localhost")
            .addSubjectAlternativeName("127.0.0.1")
            .addSubjectAlternativeName("::1")
            .build()
        val serverTls = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        val clientTls = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
        val server = MockWebServer()
        server.useHttps(serverTls.sslSocketFactory(), false)
        server.start()
        try {
            val client = OkHttpClient.Builder()
                .sslSocketFactory(clientTls.sslSocketFactory(), clientTls.trustManager)
                .build()
            block(ApiRepository(createApi(server.url("/").toString(), client)), server)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun successRespectsTheWireContract() = withApi { repository, server ->
        server.enqueue(MockResponse().setBody("""[{"id":7,"title":"Catalog item"}]"""))
        val result = repository.listItems()
        assertTrue((result as? ApiResult.Failure)?.cause.toString(), result is ApiResult.Success)
        assertEquals("Catalog item", (result as ApiResult.Success).value.single().title)
        assertEquals("/items", server.takeRequest().path)
    }

    @Test
    fun unauthorizedResponseIsAnAuthenticationFailure() = withApi { repository, server ->
        server.enqueue(MockResponse().setResponseCode(401))
        val result = repository.listItems() as ApiResult.Failure
        assertEquals(ApiResult.Kind.AUTHENTICATION, result.kind)
        assertEquals(401, result.httpCode)
    }

    @Test
    fun malformedPayloadIsAContractFailure() = withApi { repository, server ->
        server.enqueue(MockResponse().setBody("not-json"))
        val result = repository.listItems() as ApiResult.Failure
        assertEquals(ApiResult.Kind.CONTRACT, result.kind)
    }

    @Test
    fun serverFailureIsReportedWithoutAnUnboundedRetry() = withApi { repository, server ->
        server.enqueue(MockResponse().setResponseCode(500))
        val result = repository.listItems() as ApiResult.Failure
        assertEquals(ApiResult.Kind.HTTP, result.kind)
        assertEquals(500, result.httpCode)
        assertEquals(1, server.requestCount)
    }
}
'''


def extend(project):
    marker = project / '.suite-demo'
    if not marker.is_file() or marker.read_text(encoding='utf-8').strip() != 'android-development-suite-demo-v1':
        raise SuiteError('Only a marked suite demo can be extended; never target an existing product app.')
    if (project / 'feature-settings').exists() or (project / 'api-generated').exists():
        raise SuiteError('This demo was already extended; choose a fresh destination for a full rerun.')
    new_feature(argparse.Namespace(destination=project / 'feature-settings', package='com.example.suitedemo.settings', name='Settings'))
    generated = project / 'api-generated'
    generate_api(argparse.Namespace(destination=generated, package='com.example.suitedemo.data', spec=PLUGIN / 'tests/fixtures/openapi.json'))
    for source_set, files in [('main', generated.glob('*.kt')), ('test', (generated / 'tests').glob('*.kt'))]:
        target = project / f'app/src/{source_set}/java/com/example/suitedemo/data'
        target.mkdir(parents=True, exist_ok=True)
        for file in files:
            shutil.copyfile(file, target / file.name)
    (project / 'app/src/test/java/com/example/suitedemo/data/CatalogContractTest.kt').write_text(API_TEST, encoding='utf-8')
    settings = project / 'settings.gradle.kts'
    settings.write_text(settings.read_text(encoding='utf-8') + '\ninclude(":feature-settings")\n', encoding='utf-8')
    build = project / 'app/build.gradle.kts'
    text = build.read_text(encoding='utf-8').replace('plugins {', 'plugins {\n    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"', 1)
    text += '''
dependencies {
    implementation(project(":feature-settings"))
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("com.squareup.okhttp3:okhttp-tls:4.12.0")
}
'''
    build.write_text(text, encoding='utf-8')


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--destination', required=True)
    p.add_argument('--execute', action='store_true')
    p.add_argument('--extended', action='store_true', help='Include feature and HTTPS MockWebServer API checks.')
    p.add_argument('--extend-existing', action='store_true')
    args = p.parse_args()
    project = Path(args.destination).resolve()
    if not args.extend_existing:
        init_project(argparse.Namespace(destination=project, name='Suite Demo', package='com.example.suitedemo', application_id=None, min_sdk=26, target_sdk=36, compile_sdk=36))
        (project / '.suite-demo').write_text('android-development-suite-demo-v1\n', encoding='utf-8')
    if args.extended:
        extend(project)
    elif args.extend_existing:
        raise SuiteError('--extend-existing requires --extended.')
    if not args.execute:
        print(f'Scaffolded {project}; no build executed.')
        return 0
    if not (project / 'gradlew').is_file():
        wrapper = bootstrap_wrapper(project, download=True)
        write_json(project / '.android-suite/evidence/wrapper.json', wrapper)
    kinds = [('app', 'all'), ('app', 'ui-compile'), ('app', 'static')]
    if args.extended:
        kinds.append(('feature-settings', 'unit'))
    failed = False
    for module, kind in kinds:
        result = verify(project, kind, module=module, timeout=900)
        write_json(project / f'.android-suite/evidence/{module}-{kind}.json', result)
        print(f'{module}:{kind}: {result["status"]}', flush=True)
        if result['status'] != 'passed':
            failed = True
            print(result.get('reason', result['attempts'][-1]['output'][-8000:]), flush=True)
    return 1 if failed else 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except (SuiteError, OSError, ValueError) as exc:
        print(str(exc), file=sys.stderr)
        sys.exit(2)
