from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.fetch_page import FetchPage
from ...models.list_source_fetches_status_item import ListSourceFetchesStatusItem
from ...models.list_source_fetches_trigger_item import ListSourceFetchesTriggerItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    *,
    after: int | Unset = UNSET,
    limit: int | Unset = 100,
    status: list[ListSourceFetchesStatusItem] | Unset = UNSET,
    trigger: list[ListSourceFetchesTriggerItem] | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["after"] = after

    params["limit"] = limit

    json_status: list[str] | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = []
        for status_item_data in status:
            status_item = status_item_data.value
            json_status.append(status_item)

    params["status"] = json_status

    json_trigger: list[str] | Unset = UNSET
    if not isinstance(trigger, Unset):
        json_trigger = []
        for trigger_item_data in trigger:
            trigger_item = trigger_item_data.value
            json_trigger.append(trigger_item)

    params["trigger"] = json_trigger

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/sources/{source_id}/fetches".format(
            source_id=quote(str(source_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FetchPage | Problem | None:
    if response.status_code == 200:
        response_200 = FetchPage.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[FetchPage | Problem]:
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
    after: int | Unset = UNSET,
    limit: int | Unset = 100,
    status: list[ListSourceFetchesStatusItem] | Unset = UNSET,
    trigger: list[ListSourceFetchesTriggerItem] | Unset = UNSET,
) -> Response[FetchPage | Problem]:
    """Per-fetch provenance log

     Fetch-log rows of one source (scheduled, manual, stream flushes and pushes), newest first, keyset-
    paged on id. Rows carry counts, seq range, redacted URL, ETag, body hash and licence; never request
    or response bodies.

    **Scope:** `admin`.

    Args:
        source_id (str):
        after (int | Unset):
        limit (int | Unset):  Default: 100.
        status (list[ListSourceFetchesStatusItem] | Unset):
        trigger (list[ListSourceFetchesTriggerItem] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FetchPage | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        after=after,
        limit=limit,
        status=status,
        trigger=trigger,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    limit: int | Unset = 100,
    status: list[ListSourceFetchesStatusItem] | Unset = UNSET,
    trigger: list[ListSourceFetchesTriggerItem] | Unset = UNSET,
) -> FetchPage | Problem | None:
    """Per-fetch provenance log

     Fetch-log rows of one source (scheduled, manual, stream flushes and pushes), newest first, keyset-
    paged on id. Rows carry counts, seq range, redacted URL, ETag, body hash and licence; never request
    or response bodies.

    **Scope:** `admin`.

    Args:
        source_id (str):
        after (int | Unset):
        limit (int | Unset):  Default: 100.
        status (list[ListSourceFetchesStatusItem] | Unset):
        trigger (list[ListSourceFetchesTriggerItem] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FetchPage | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        after=after,
        limit=limit,
        status=status,
        trigger=trigger,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    limit: int | Unset = 100,
    status: list[ListSourceFetchesStatusItem] | Unset = UNSET,
    trigger: list[ListSourceFetchesTriggerItem] | Unset = UNSET,
) -> Response[FetchPage | Problem]:
    """Per-fetch provenance log

     Fetch-log rows of one source (scheduled, manual, stream flushes and pushes), newest first, keyset-
    paged on id. Rows carry counts, seq range, redacted URL, ETag, body hash and licence; never request
    or response bodies.

    **Scope:** `admin`.

    Args:
        source_id (str):
        after (int | Unset):
        limit (int | Unset):  Default: 100.
        status (list[ListSourceFetchesStatusItem] | Unset):
        trigger (list[ListSourceFetchesTriggerItem] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FetchPage | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        after=after,
        limit=limit,
        status=status,
        trigger=trigger,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    limit: int | Unset = 100,
    status: list[ListSourceFetchesStatusItem] | Unset = UNSET,
    trigger: list[ListSourceFetchesTriggerItem] | Unset = UNSET,
) -> FetchPage | Problem | None:
    """Per-fetch provenance log

     Fetch-log rows of one source (scheduled, manual, stream flushes and pushes), newest first, keyset-
    paged on id. Rows carry counts, seq range, redacted URL, ETag, body hash and licence; never request
    or response bodies.

    **Scope:** `admin`.

    Args:
        source_id (str):
        after (int | Unset):
        limit (int | Unset):  Default: 100.
        status (list[ListSourceFetchesStatusItem] | Unset):
        trigger (list[ListSourceFetchesTriggerItem] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FetchPage | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            after=after,
            limit=limit,
            status=status,
            trigger=trigger,
        )
    ).parsed
