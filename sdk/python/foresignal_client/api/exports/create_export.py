from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.export_job import ExportJob
from ...models.export_spec import ExportSpec
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ExportSpec,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/exports",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExportJob | Problem | None:
    if response.status_code == 202:
        response_202 = ExportJob.from_dict(response.json())

        return response_202

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExportJob | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ExportSpec,
) -> Response[ExportJob | Problem]:
    """Start a bulk export

     Queues an async export (GeoParquet 1.1 by default, GeoPackage 1.4, FlatGeobuf or GPX for history).
    Rows stream with keyset reads; the job writes a licence sidecar and a signed SHA-256 manifest.
    FORBIDDEN and LOCAL_ONLY records are excluded unless `personalUse=true`; private sources need admin
    and `includePrivate=true`. Errors: `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`
    (exports.diskQuotaGb).

    **Scope:** `agent`.

    Args:
        body (ExportSpec): Bulk export request (111). FORBIDDEN and LOCAL_ONLY records are
            excluded unless personalUse=true

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExportJob | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ExportSpec,
) -> ExportJob | Problem | None:
    """Start a bulk export

     Queues an async export (GeoParquet 1.1 by default, GeoPackage 1.4, FlatGeobuf or GPX for history).
    Rows stream with keyset reads; the job writes a licence sidecar and a signed SHA-256 manifest.
    FORBIDDEN and LOCAL_ONLY records are excluded unless `personalUse=true`; private sources need admin
    and `includePrivate=true`. Errors: `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`
    (exports.diskQuotaGb).

    **Scope:** `agent`.

    Args:
        body (ExportSpec): Bulk export request (111). FORBIDDEN and LOCAL_ONLY records are
            excluded unless personalUse=true

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExportJob | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ExportSpec,
) -> Response[ExportJob | Problem]:
    """Start a bulk export

     Queues an async export (GeoParquet 1.1 by default, GeoPackage 1.4, FlatGeobuf or GPX for history).
    Rows stream with keyset reads; the job writes a licence sidecar and a signed SHA-256 manifest.
    FORBIDDEN and LOCAL_ONLY records are excluded unless `personalUse=true`; private sources need admin
    and `includePrivate=true`. Errors: `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`
    (exports.diskQuotaGb).

    **Scope:** `agent`.

    Args:
        body (ExportSpec): Bulk export request (111). FORBIDDEN and LOCAL_ONLY records are
            excluded unless personalUse=true

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExportJob | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ExportSpec,
) -> ExportJob | Problem | None:
    """Start a bulk export

     Queues an async export (GeoParquet 1.1 by default, GeoPackage 1.4, FlatGeobuf or GPX for history).
    Rows stream with keyset reads; the job writes a licence sidecar and a signed SHA-256 manifest.
    FORBIDDEN and LOCAL_ONLY records are excluded unless `personalUse=true`; private sources need admin
    and `includePrivate=true`. Errors: `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`
    (exports.diskQuotaGb).

    **Scope:** `agent`.

    Args:
        body (ExportSpec): Bulk export request (111). FORBIDDEN and LOCAL_ONLY records are
            excluded unless personalUse=true

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExportJob | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
