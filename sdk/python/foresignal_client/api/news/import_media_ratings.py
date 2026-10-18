from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.import_media_ratings_files_body import ImportMediaRatingsFilesBody
from ...models.import_media_ratings_json_body import ImportMediaRatingsJsonBody
from ...models.import_media_ratings_provider import ImportMediaRatingsProvider
from ...models.problem import Problem
from ...models.ratings_import_result import RatingsImportResult
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    body: ImportMediaRatingsFilesBody | ImportMediaRatingsJsonBody | Unset = UNSET,
    provider: ImportMediaRatingsProvider,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    params: dict[str, Any] = {}

    json_provider = provider.value
    params["provider"] = json_provider

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/media/ratings/import",
        "params": params,
    }

    if isinstance(body, ImportMediaRatingsFilesBody):
        if not isinstance(body, Unset):
            _kwargs["files"] = body.to_multipart()

        headers["Content-Type"] = "multipart/form-data; boundary=+++"
    if isinstance(body, ImportMediaRatingsJsonBody):
        if not isinstance(body, Unset):
            _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RatingsImportResult | None:
    if response.status_code == 200:
        response_200 = RatingsImportResult.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | RatingsImportResult]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ImportMediaRatingsFilesBody | ImportMediaRatingsJsonBody | Unset = UNSET,
    provider: ImportMediaRatingsProvider,
) -> Response[Problem | RatingsImportResult]:
    """Import media ratings

     Runs a provider import (`wikidata`, `partisan-web`, `allsides`, `mbfc`) or an operator CSV/JSON file
    (`domain, dimension, value, label, licence, attribution`). Consensus is recomputed after import.

    **Scope:** `admin`.

    Args:
        provider (ImportMediaRatingsProvider):
        body (ImportMediaRatingsFilesBody | Unset): Operator file upload
        body (ImportMediaRatingsJsonBody | Unset): Operator rows

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RatingsImportResult]
    """

    kwargs = _get_kwargs(
        body=body,
        provider=provider,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ImportMediaRatingsFilesBody | ImportMediaRatingsJsonBody | Unset = UNSET,
    provider: ImportMediaRatingsProvider,
) -> Problem | RatingsImportResult | None:
    """Import media ratings

     Runs a provider import (`wikidata`, `partisan-web`, `allsides`, `mbfc`) or an operator CSV/JSON file
    (`domain, dimension, value, label, licence, attribution`). Consensus is recomputed after import.

    **Scope:** `admin`.

    Args:
        provider (ImportMediaRatingsProvider):
        body (ImportMediaRatingsFilesBody | Unset): Operator file upload
        body (ImportMediaRatingsJsonBody | Unset): Operator rows

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RatingsImportResult
    """

    return sync_detailed(
        client=client,
        body=body,
        provider=provider,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ImportMediaRatingsFilesBody | ImportMediaRatingsJsonBody | Unset = UNSET,
    provider: ImportMediaRatingsProvider,
) -> Response[Problem | RatingsImportResult]:
    """Import media ratings

     Runs a provider import (`wikidata`, `partisan-web`, `allsides`, `mbfc`) or an operator CSV/JSON file
    (`domain, dimension, value, label, licence, attribution`). Consensus is recomputed after import.

    **Scope:** `admin`.

    Args:
        provider (ImportMediaRatingsProvider):
        body (ImportMediaRatingsFilesBody | Unset): Operator file upload
        body (ImportMediaRatingsJsonBody | Unset): Operator rows

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RatingsImportResult]
    """

    kwargs = _get_kwargs(
        body=body,
        provider=provider,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ImportMediaRatingsFilesBody | ImportMediaRatingsJsonBody | Unset = UNSET,
    provider: ImportMediaRatingsProvider,
) -> Problem | RatingsImportResult | None:
    """Import media ratings

     Runs a provider import (`wikidata`, `partisan-web`, `allsides`, `mbfc`) or an operator CSV/JSON file
    (`domain, dimension, value, label, licence, attribution`). Consensus is recomputed after import.

    **Scope:** `admin`.

    Args:
        provider (ImportMediaRatingsProvider):
        body (ImportMediaRatingsFilesBody | Unset): Operator file upload
        body (ImportMediaRatingsJsonBody | Unset): Operator rows

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RatingsImportResult
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
            provider=provider,
        )
    ).parsed
