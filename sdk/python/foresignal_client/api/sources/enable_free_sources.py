from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.enable_free_sources_result import EnableFreeSourcesResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/sources/enable-free",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EnableFreeSourcesResult | Problem | None:
    if response.status_code == 200:
        response_200 = EnableFreeSourcesResult.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[EnableFreeSourcesResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[EnableFreeSourcesResult | Problem]:
    """Enable every free source

     Enables every currently disabled built-in source that runs without operator input: no required
    credential, no required parameter, and parameters that are valid with nothing supplied (the same
    checks `PUT /v1/sources/{sourceId}` applies when enabling), unless the source is held off by default
    for its bandwidth or terms (prompt 146: `wiki-edit-surge`, which the operator enables with `PUT
    /v1/sources/wiki-edit-surge`). Each source is enabled through the settings-update path, so the
    collector picks it up within seconds and fetches it at its default interval and budget. Enabled
    sources, credential- or parameter-gated sources, held-off sources and custom or external sources are
    left untouched. Idempotent: a second call enables nothing. A fresh install seeds only the core set
    enabled (prompt 150, `enableCoreSources`); this call turns on every free source on top of it.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EnableFreeSourcesResult | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> EnableFreeSourcesResult | Problem | None:
    """Enable every free source

     Enables every currently disabled built-in source that runs without operator input: no required
    credential, no required parameter, and parameters that are valid with nothing supplied (the same
    checks `PUT /v1/sources/{sourceId}` applies when enabling), unless the source is held off by default
    for its bandwidth or terms (prompt 146: `wiki-edit-surge`, which the operator enables with `PUT
    /v1/sources/wiki-edit-surge`). Each source is enabled through the settings-update path, so the
    collector picks it up within seconds and fetches it at its default interval and budget. Enabled
    sources, credential- or parameter-gated sources, held-off sources and custom or external sources are
    left untouched. Idempotent: a second call enables nothing. A fresh install seeds only the core set
    enabled (prompt 150, `enableCoreSources`); this call turns on every free source on top of it.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EnableFreeSourcesResult | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[EnableFreeSourcesResult | Problem]:
    """Enable every free source

     Enables every currently disabled built-in source that runs without operator input: no required
    credential, no required parameter, and parameters that are valid with nothing supplied (the same
    checks `PUT /v1/sources/{sourceId}` applies when enabling), unless the source is held off by default
    for its bandwidth or terms (prompt 146: `wiki-edit-surge`, which the operator enables with `PUT
    /v1/sources/wiki-edit-surge`). Each source is enabled through the settings-update path, so the
    collector picks it up within seconds and fetches it at its default interval and budget. Enabled
    sources, credential- or parameter-gated sources, held-off sources and custom or external sources are
    left untouched. Idempotent: a second call enables nothing. A fresh install seeds only the core set
    enabled (prompt 150, `enableCoreSources`); this call turns on every free source on top of it.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EnableFreeSourcesResult | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> EnableFreeSourcesResult | Problem | None:
    """Enable every free source

     Enables every currently disabled built-in source that runs without operator input: no required
    credential, no required parameter, and parameters that are valid with nothing supplied (the same
    checks `PUT /v1/sources/{sourceId}` applies when enabling), unless the source is held off by default
    for its bandwidth or terms (prompt 146: `wiki-edit-surge`, which the operator enables with `PUT
    /v1/sources/wiki-edit-surge`). Each source is enabled through the settings-update path, so the
    collector picks it up within seconds and fetches it at its default interval and budget. Enabled
    sources, credential- or parameter-gated sources, held-off sources and custom or external sources are
    left untouched. Idempotent: a second call enables nothing. A fresh install seeds only the core set
    enabled (prompt 150, `enableCoreSources`); this call turns on every free source on top of it.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EnableFreeSourcesResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
