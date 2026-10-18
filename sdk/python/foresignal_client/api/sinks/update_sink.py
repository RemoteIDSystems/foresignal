from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.sink_update import SinkUpdate
from ...types import Response


def _get_kwargs(
    sink_id: str,
    *,
    body: SinkUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/sinks/{sink_id}".format(
            sink_id=quote(str(sink_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    sink_id: str,
    *,
    client: AuthenticatedClient,
    body: SinkUpdate,
) -> Response[Problem]:
    """Replace a sink's settings

     Replaces name, enabled, config and filter; omitted secrets keep their stored values. Enabling starts
    the sink on its own virtual thread; disabling stops it. Same validation as create. Audited.

    **Scope:** `admin`.

    Args:
        sink_id (str):
        body (SinkUpdate): Replace a sink's settings. Secrets are merged, not replaced: a field
            with a value sets it, a field set to null removes it, an omitted field keeps its stored
            value, and `"secrets": null` removes every secret (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        sink_id=sink_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    sink_id: str,
    *,
    client: AuthenticatedClient,
    body: SinkUpdate,
) -> Problem | None:
    """Replace a sink's settings

     Replaces name, enabled, config and filter; omitted secrets keep their stored values. Enabling starts
    the sink on its own virtual thread; disabling stops it. Same validation as create. Audited.

    **Scope:** `admin`.

    Args:
        sink_id (str):
        body (SinkUpdate): Replace a sink's settings. Secrets are merged, not replaced: a field
            with a value sets it, a field set to null removes it, an omitted field keeps its stored
            value, and `"secrets": null` removes every secret (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        sink_id=sink_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    sink_id: str,
    *,
    client: AuthenticatedClient,
    body: SinkUpdate,
) -> Response[Problem]:
    """Replace a sink's settings

     Replaces name, enabled, config and filter; omitted secrets keep their stored values. Enabling starts
    the sink on its own virtual thread; disabling stops it. Same validation as create. Audited.

    **Scope:** `admin`.

    Args:
        sink_id (str):
        body (SinkUpdate): Replace a sink's settings. Secrets are merged, not replaced: a field
            with a value sets it, a field set to null removes it, an omitted field keeps its stored
            value, and `"secrets": null` removes every secret (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        sink_id=sink_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    sink_id: str,
    *,
    client: AuthenticatedClient,
    body: SinkUpdate,
) -> Problem | None:
    """Replace a sink's settings

     Replaces name, enabled, config and filter; omitted secrets keep their stored values. Enabling starts
    the sink on its own virtual thread; disabling stops it. Same validation as create. Audited.

    **Scope:** `admin`.

    Args:
        sink_id (str):
        body (SinkUpdate): Replace a sink's settings. Secrets are merged, not replaced: a field
            with a value sets it, a field set to null removes it, an omitted field keeps its stored
            value, and `"secrets": null` removes every secret (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            sink_id=sink_id,
            client=client,
            body=body,
        )
    ).parsed
