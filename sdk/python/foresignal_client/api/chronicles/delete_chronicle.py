from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response


def _get_kwargs(
    chronicle_id: str,
    *,
    confirm: str,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["confirm"] = confirm

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/chronicles/{chronicle_id}".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 204:
        response_204 = cast(Any, None)
        return response_204

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
) -> Response[Any | Problem]:
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
    confirm: str,
) -> Response[Any | Problem]:
    """Delete a chronicle

     Drops the chronicle's storage leaf and snapshots (owner only). Requires the confirmation token
    `confirm` equal to the chronicle's display id.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        confirm (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        confirm=confirm,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    confirm: str,
) -> Any | Problem | None:
    """Delete a chronicle

     Drops the chronicle's storage leaf and snapshots (owner only). Requires the confirmation token
    `confirm` equal to the chronicle's display id.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        confirm (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        confirm=confirm,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    confirm: str,
) -> Response[Any | Problem]:
    """Delete a chronicle

     Drops the chronicle's storage leaf and snapshots (owner only). Requires the confirmation token
    `confirm` equal to the chronicle's display id.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        confirm (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        confirm=confirm,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    confirm: str,
) -> Any | Problem | None:
    """Delete a chronicle

     Drops the chronicle's storage leaf and snapshots (owner only). Requires the confirmation token
    `confirm` equal to the chronicle's display id.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        confirm (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            confirm=confirm,
        )
    ).parsed
