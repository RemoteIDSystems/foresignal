from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.mapping_preview_request import MappingPreviewRequest
from ...models.mapping_preview_result import MappingPreviewResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: MappingPreviewRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/mappings/preview",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MappingPreviewResult | Problem | None:
    if response.status_code == 200:
        response_200 = MappingPreviewResult.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[MappingPreviewResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: MappingPreviewRequest,
) -> Response[MappingPreviewResult | Problem]:
    """Preview a RecordMapping over a sample

     Compiles the mapping with `MappingCompiler` and applies it to the sample through the standard 008
    normaliser, returning exactly what ingest would write. Writes nothing. Warnings cover unmapped input
    keys, fields missing from the source's `fieldSpecs`, a policy-required significant field that is
    missing, and a time-base mismatch with the source's `observedAtMode`. Compile errors return 400
    `MAPPING_INVALID` with `errors[].field` paths such as `mapping.fields[3].unit.from`.

    **Scope:** `admin`.

    Args:
        body (MappingPreviewRequest): Preview request: a mapping and a sample, optionally checked
            against an existing custom source (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MappingPreviewResult | Problem]
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
    body: MappingPreviewRequest,
) -> MappingPreviewResult | Problem | None:
    """Preview a RecordMapping over a sample

     Compiles the mapping with `MappingCompiler` and applies it to the sample through the standard 008
    normaliser, returning exactly what ingest would write. Writes nothing. Warnings cover unmapped input
    keys, fields missing from the source's `fieldSpecs`, a policy-required significant field that is
    missing, and a time-base mismatch with the source's `observedAtMode`. Compile errors return 400
    `MAPPING_INVALID` with `errors[].field` paths such as `mapping.fields[3].unit.from`.

    **Scope:** `admin`.

    Args:
        body (MappingPreviewRequest): Preview request: a mapping and a sample, optionally checked
            against an existing custom source (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MappingPreviewResult | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: MappingPreviewRequest,
) -> Response[MappingPreviewResult | Problem]:
    """Preview a RecordMapping over a sample

     Compiles the mapping with `MappingCompiler` and applies it to the sample through the standard 008
    normaliser, returning exactly what ingest would write. Writes nothing. Warnings cover unmapped input
    keys, fields missing from the source's `fieldSpecs`, a policy-required significant field that is
    missing, and a time-base mismatch with the source's `observedAtMode`. Compile errors return 400
    `MAPPING_INVALID` with `errors[].field` paths such as `mapping.fields[3].unit.from`.

    **Scope:** `admin`.

    Args:
        body (MappingPreviewRequest): Preview request: a mapping and a sample, optionally checked
            against an existing custom source (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MappingPreviewResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: MappingPreviewRequest,
) -> MappingPreviewResult | Problem | None:
    """Preview a RecordMapping over a sample

     Compiles the mapping with `MappingCompiler` and applies it to the sample through the standard 008
    normaliser, returning exactly what ingest would write. Writes nothing. Warnings cover unmapped input
    keys, fields missing from the source's `fieldSpecs`, a policy-required significant field that is
    missing, and a time-base mismatch with the source's `observedAtMode`. Compile errors return 400
    `MAPPING_INVALID` with `errors[].field` paths such as `mapping.fields[3].unit.from`.

    **Scope:** `admin`.

    Args:
        body (MappingPreviewRequest): Preview request: a mapping and a sample, optionally checked
            against an existing custom source (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MappingPreviewResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
