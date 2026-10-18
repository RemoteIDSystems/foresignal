from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analyst_proposal_page import AnalystProposalPage
from ...models.list_analyst_proposals_status import ListAnalystProposalsStatus
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: ListAnalystProposalsStatus | Unset = UNSET,
    profile_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    params["profileId"] = profile_id

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analyst/proposals",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalystProposalPage | Problem | None:
    if response.status_code == 200:
        response_200 = AnalystProposalPage.from_dict(response.json())

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
) -> Response[AnalystProposalPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: ListAnalystProposalsStatus | Unset = UNSET,
    profile_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AnalystProposalPage | Problem]:
    """List analyst proposals

     Pending proposals expire after 7 days.

    **Scope:** `agent` or `admin`.

    Args:
        status (ListAnalystProposalsStatus | Unset):
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystProposalPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        profile_id=profile_id,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    status: ListAnalystProposalsStatus | Unset = UNSET,
    profile_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AnalystProposalPage | Problem | None:
    """List analyst proposals

     Pending proposals expire after 7 days.

    **Scope:** `agent` or `admin`.

    Args:
        status (ListAnalystProposalsStatus | Unset):
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystProposalPage | Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        profile_id=profile_id,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: ListAnalystProposalsStatus | Unset = UNSET,
    profile_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AnalystProposalPage | Problem]:
    """List analyst proposals

     Pending proposals expire after 7 days.

    **Scope:** `agent` or `admin`.

    Args:
        status (ListAnalystProposalsStatus | Unset):
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystProposalPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        profile_id=profile_id,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: ListAnalystProposalsStatus | Unset = UNSET,
    profile_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AnalystProposalPage | Problem | None:
    """List analyst proposals

     Pending proposals expire after 7 days.

    **Scope:** `agent` or `admin`.

    Args:
        status (ListAnalystProposalsStatus | Unset):
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystProposalPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            profile_id=profile_id,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
