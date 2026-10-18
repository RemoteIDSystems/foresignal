from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.discoverability import Discoverability
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    chronicle_id: str,
    *,
    body: Discoverability,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/chronicles/{chronicle_id}/discoverability".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Discoverability | Problem | None:
    if response.status_code == 200:
        response_200 = Discoverability.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Discoverability | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: Discoverability,
) -> Response[Discoverability | Problem]:
    """Set chronicle discoverability

     Sets discoverability and reveal flags (owner only). TLP caps it: RED → none, AMBER+STRICT → none or
    instance; a stricter label set later lowers it automatically (logged, owner notified with
    `chronicle.discoverability_lowered`). Lowering it from `network` withdraws the chronicle's sketch
    from peers at the next exchange.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        body (Discoverability): A chronicle's discoverability setting (134); TLP caps it (RED →
            none; AMBER+STRICT → none | instance)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Discoverability | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: Discoverability,
) -> Discoverability | Problem | None:
    """Set chronicle discoverability

     Sets discoverability and reveal flags (owner only). TLP caps it: RED → none, AMBER+STRICT → none or
    instance; a stricter label set later lowers it automatically (logged, owner notified with
    `chronicle.discoverability_lowered`). Lowering it from `network` withdraws the chronicle's sketch
    from peers at the next exchange.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        body (Discoverability): A chronicle's discoverability setting (134); TLP caps it (RED →
            none; AMBER+STRICT → none | instance)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Discoverability | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: Discoverability,
) -> Response[Discoverability | Problem]:
    """Set chronicle discoverability

     Sets discoverability and reveal flags (owner only). TLP caps it: RED → none, AMBER+STRICT → none or
    instance; a stricter label set later lowers it automatically (logged, owner notified with
    `chronicle.discoverability_lowered`). Lowering it from `network` withdraws the chronicle's sketch
    from peers at the next exchange.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        body (Discoverability): A chronicle's discoverability setting (134); TLP caps it (RED →
            none; AMBER+STRICT → none | instance)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Discoverability | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: Discoverability,
) -> Discoverability | Problem | None:
    """Set chronicle discoverability

     Sets discoverability and reveal flags (owner only). TLP caps it: RED → none, AMBER+STRICT → none or
    instance; a stricter label set later lowers it automatically (logged, owner notified with
    `chronicle.discoverability_lowered`). Lowering it from `network` withdraws the chronicle's sketch
    from peers at the next exchange.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        body (Discoverability): A chronicle's discoverability setting (134); TLP caps it (RED →
            none; AMBER+STRICT → none | instance)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Discoverability | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            body=body,
        )
    ).parsed
