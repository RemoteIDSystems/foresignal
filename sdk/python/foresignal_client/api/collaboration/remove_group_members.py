from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.group import Group
from ...models.problem import Problem
from ...types import UNSET, Response


def _get_kwargs(
    group_id: str,
    *,
    principal_id: list[str],
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_principal_id = principal_id

    params["principalId"] = json_principal_id

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/groups/{group_id}/members".format(
            group_id=quote(str(group_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Group | Problem | None:
    if response.status_code == 200:
        response_200 = Group.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Group | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    group_id: str,
    *,
    client: AuthenticatedClient,
    principal_id: list[str],
) -> Response[Group | Problem]:
    """Remove group members

     Removes the listed principals from the group (managers or admin; principals may remove themselves).
    Audited.

    **Scope:** `agent`.

    Args:
        group_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        principal_id (list[str]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Group | Problem]
    """

    kwargs = _get_kwargs(
        group_id=group_id,
        principal_id=principal_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    group_id: str,
    *,
    client: AuthenticatedClient,
    principal_id: list[str],
) -> Group | Problem | None:
    """Remove group members

     Removes the listed principals from the group (managers or admin; principals may remove themselves).
    Audited.

    **Scope:** `agent`.

    Args:
        group_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        principal_id (list[str]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Group | Problem
    """

    return sync_detailed(
        group_id=group_id,
        client=client,
        principal_id=principal_id,
    ).parsed


async def asyncio_detailed(
    group_id: str,
    *,
    client: AuthenticatedClient,
    principal_id: list[str],
) -> Response[Group | Problem]:
    """Remove group members

     Removes the listed principals from the group (managers or admin; principals may remove themselves).
    Audited.

    **Scope:** `agent`.

    Args:
        group_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        principal_id (list[str]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Group | Problem]
    """

    kwargs = _get_kwargs(
        group_id=group_id,
        principal_id=principal_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    group_id: str,
    *,
    client: AuthenticatedClient,
    principal_id: list[str],
) -> Group | Problem | None:
    """Remove group members

     Removes the listed principals from the group (managers or admin; principals may remove themselves).
    Audited.

    **Scope:** `agent`.

    Args:
        group_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        principal_id (list[str]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Group | Problem
    """

    return (
        await asyncio_detailed(
            group_id=group_id,
            client=client,
            principal_id=principal_id,
        )
    ).parsed
