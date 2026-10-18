from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.edp_manifest import EdpManifest
from ...models.external_source import ExternalSource
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: EdpManifest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/external/sources/{source_id}/manifest".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExternalSource | Problem | None:
    if response.status_code == 200:
        response_200 = ExternalSource.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExternalSource | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: EdpManifest,
) -> Response[ExternalSource | Problem]:
    """Update a source manifest

     Additive changes apply at once; changes to streams, kinds, item types, modes, update styles,
    geometry types, payload schemas or provider URLs (pull itemsUrl, search url) return the source to
    `pending`; the response lists them in `breakingChanges`. Accepted from an admin token or the
    source's own ingest key.

    **Scope:** ingest key of the source (or `admin`).

    Args:
        source_id (str):
        body (EdpManifest): Self-description of an EDP provider (docs/External-Data-Protocol.md
            §2). Stream ids must be unique and each layer stream must carry feature or event items
            (checked at registration). Unknown fields are ignored at every level (forward
            compatibility within 1.x).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalSource | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: EdpManifest,
) -> ExternalSource | Problem | None:
    """Update a source manifest

     Additive changes apply at once; changes to streams, kinds, item types, modes, update styles,
    geometry types, payload schemas or provider URLs (pull itemsUrl, search url) return the source to
    `pending`; the response lists them in `breakingChanges`. Accepted from an admin token or the
    source's own ingest key.

    **Scope:** ingest key of the source (or `admin`).

    Args:
        source_id (str):
        body (EdpManifest): Self-description of an EDP provider (docs/External-Data-Protocol.md
            §2). Stream ids must be unique and each layer stream must carry feature or event items
            (checked at registration). Unknown fields are ignored at every level (forward
            compatibility within 1.x).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalSource | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: EdpManifest,
) -> Response[ExternalSource | Problem]:
    """Update a source manifest

     Additive changes apply at once; changes to streams, kinds, item types, modes, update styles,
    geometry types, payload schemas or provider URLs (pull itemsUrl, search url) return the source to
    `pending`; the response lists them in `breakingChanges`. Accepted from an admin token or the
    source's own ingest key.

    **Scope:** ingest key of the source (or `admin`).

    Args:
        source_id (str):
        body (EdpManifest): Self-description of an EDP provider (docs/External-Data-Protocol.md
            §2). Stream ids must be unique and each layer stream must carry feature or event items
            (checked at registration). Unknown fields are ignored at every level (forward
            compatibility within 1.x).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalSource | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: EdpManifest,
) -> ExternalSource | Problem | None:
    """Update a source manifest

     Additive changes apply at once; changes to streams, kinds, item types, modes, update styles,
    geometry types, payload schemas or provider URLs (pull itemsUrl, search url) return the source to
    `pending`; the response lists them in `breakingChanges`. Accepted from an admin token or the
    source's own ingest key.

    **Scope:** ingest key of the source (or `admin`).

    Args:
        source_id (str):
        body (EdpManifest): Self-description of an EDP provider (docs/External-Data-Protocol.md
            §2). Stream ids must be unique and each layer stream must carry feature or event items
            (checked at registration). Unknown fields are ignored at every level (forward
            compatibility within 1.x).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalSource | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
