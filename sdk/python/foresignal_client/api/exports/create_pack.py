from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.export_job import ExportJob
from ...models.pack_spec import PackSpec
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: PackSpec,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/packs",
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

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

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
    body: PackSpec,
) -> Response[ExportJob | Problem]:
    """Build a PMTiles offline pack

     Queues a PMTiles v3 pack job (kind PACK): one .pmtiles per tile type (MVT from 016/037, PNG from
    045), deduplicated, with 109 attribution in the metadata. Download through
    `/v1/exports/{id}/download`. Licence and private rules as exports (EXPORT egress). Errors: `413
    PACK_TOO_LARGE` (more than packs.maxTiles, 200,000; the problem's `tileCount` states the computed
    count), `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`.

    **Scope:** `agent`.

    Args:
        body (PackSpec): PMTiles v3 offline pack request: one .pmtiles per tile type (MVT and/or
            PNG) (111)

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
    body: PackSpec,
) -> ExportJob | Problem | None:
    """Build a PMTiles offline pack

     Queues a PMTiles v3 pack job (kind PACK): one .pmtiles per tile type (MVT from 016/037, PNG from
    045), deduplicated, with 109 attribution in the metadata. Download through
    `/v1/exports/{id}/download`. Licence and private rules as exports (EXPORT egress). Errors: `413
    PACK_TOO_LARGE` (more than packs.maxTiles, 200,000; the problem's `tileCount` states the computed
    count), `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`.

    **Scope:** `agent`.

    Args:
        body (PackSpec): PMTiles v3 offline pack request: one .pmtiles per tile type (MVT and/or
            PNG) (111)

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
    body: PackSpec,
) -> Response[ExportJob | Problem]:
    """Build a PMTiles offline pack

     Queues a PMTiles v3 pack job (kind PACK): one .pmtiles per tile type (MVT from 016/037, PNG from
    045), deduplicated, with 109 attribution in the metadata. Download through
    `/v1/exports/{id}/download`. Licence and private rules as exports (EXPORT egress). Errors: `413
    PACK_TOO_LARGE` (more than packs.maxTiles, 200,000; the problem's `tileCount` states the computed
    count), `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`.

    **Scope:** `agent`.

    Args:
        body (PackSpec): PMTiles v3 offline pack request: one .pmtiles per tile type (MVT and/or
            PNG) (111)

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
    body: PackSpec,
) -> ExportJob | Problem | None:
    """Build a PMTiles offline pack

     Queues a PMTiles v3 pack job (kind PACK): one .pmtiles per tile type (MVT from 016/037, PNG from
    045), deduplicated, with 109 attribution in the metadata. Download through
    `/v1/exports/{id}/download`. Licence and private rules as exports (EXPORT egress). Errors: `413
    PACK_TOO_LARGE` (more than packs.maxTiles, 200,000; the problem's `tileCount` states the computed
    count), `400 EXPORT_SPEC_INVALID`, `409 EXPORT_QUOTA`.

    **Scope:** `agent`.

    Args:
        body (PackSpec): PMTiles v3 offline pack request: one .pmtiles per tile type (MVT and/or
            PNG) (111)

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
