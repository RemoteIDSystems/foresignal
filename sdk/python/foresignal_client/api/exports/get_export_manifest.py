from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.export_manifest import ExportManifest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    export_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/exports/{export_id}/manifest".format(
            export_id=quote(str(export_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExportManifest | Problem | None:
    if response.status_code == 200:
        response_200 = ExportManifest.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExportManifest | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    export_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExportManifest | Problem]:
    """Get an export's signed manifest

     The SHA-256 manifest; the `Foresignal-Signature` header carries the detached JWS (also in the bundle
    as `manifest.json.jws`), verifiable against the JWKS. Errors: `410 EXPORT_EXPIRED`; `409
    EXPORT_NOT_READY` while the job is not DONE.

    **Scope:** `agent`.

    Args:
        export_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExportManifest | Problem]
    """

    kwargs = _get_kwargs(
        export_id=export_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    export_id: str,
    *,
    client: AuthenticatedClient,
) -> ExportManifest | Problem | None:
    """Get an export's signed manifest

     The SHA-256 manifest; the `Foresignal-Signature` header carries the detached JWS (also in the bundle
    as `manifest.json.jws`), verifiable against the JWKS. Errors: `410 EXPORT_EXPIRED`; `409
    EXPORT_NOT_READY` while the job is not DONE.

    **Scope:** `agent`.

    Args:
        export_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExportManifest | Problem
    """

    return sync_detailed(
        export_id=export_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    export_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExportManifest | Problem]:
    """Get an export's signed manifest

     The SHA-256 manifest; the `Foresignal-Signature` header carries the detached JWS (also in the bundle
    as `manifest.json.jws`), verifiable against the JWKS. Errors: `410 EXPORT_EXPIRED`; `409
    EXPORT_NOT_READY` while the job is not DONE.

    **Scope:** `agent`.

    Args:
        export_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExportManifest | Problem]
    """

    kwargs = _get_kwargs(
        export_id=export_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    export_id: str,
    *,
    client: AuthenticatedClient,
) -> ExportManifest | Problem | None:
    """Get an export's signed manifest

     The SHA-256 manifest; the `Foresignal-Signature` header carries the detached JWS (also in the bundle
    as `manifest.json.jws`), verifiable against the JWKS. Errors: `410 EXPORT_EXPIRED`; `409
    EXPORT_NOT_READY` while the job is not DONE.

    **Scope:** `agent`.

    Args:
        export_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExportManifest | Problem
    """

    return (
        await asyncio_detailed(
            export_id=export_id,
            client=client,
        )
    ).parsed
