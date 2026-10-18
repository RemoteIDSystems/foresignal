from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_media_outlets_bucket import ListMediaOutletsBucket
from ...models.list_media_outlets_type import ListMediaOutletsType
from ...models.media_outlet_page import MediaOutletPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    q: str | Unset = UNSET,
    country: str | Unset = UNSET,
    type_: ListMediaOutletsType | Unset = UNSET,
    bucket: ListMediaOutletsBucket | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["q"] = q

    params["country"] = country

    json_type_: str | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = type_.value

    params["type"] = json_type_

    json_bucket: str | Unset = UNSET
    if not isinstance(bucket, Unset):
        json_bucket = bucket.value

    params["bucket"] = json_bucket

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/media/outlets",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MediaOutletPage | Problem | None:
    if response.status_code == 200:
        response_200 = MediaOutletPage.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[MediaOutletPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    country: str | Unset = UNSET,
    type_: ListMediaOutletsType | Unset = UNSET,
    bucket: ListMediaOutletsBucket | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[MediaOutletPage | Problem]:
    """List media outlets

     Outlet registry with consensus per dimension.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str | Unset):
        country (str | Unset):
        type_ (ListMediaOutletsType | Unset):
        bucket (ListMediaOutletsBucket | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaOutletPage | Problem]
    """

    kwargs = _get_kwargs(
        q=q,
        country=country,
        type_=type_,
        bucket=bucket,
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
    q: str | Unset = UNSET,
    country: str | Unset = UNSET,
    type_: ListMediaOutletsType | Unset = UNSET,
    bucket: ListMediaOutletsBucket | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> MediaOutletPage | Problem | None:
    """List media outlets

     Outlet registry with consensus per dimension.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str | Unset):
        country (str | Unset):
        type_ (ListMediaOutletsType | Unset):
        bucket (ListMediaOutletsBucket | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaOutletPage | Problem
    """

    return sync_detailed(
        client=client,
        q=q,
        country=country,
        type_=type_,
        bucket=bucket,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    country: str | Unset = UNSET,
    type_: ListMediaOutletsType | Unset = UNSET,
    bucket: ListMediaOutletsBucket | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[MediaOutletPage | Problem]:
    """List media outlets

     Outlet registry with consensus per dimension.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str | Unset):
        country (str | Unset):
        type_ (ListMediaOutletsType | Unset):
        bucket (ListMediaOutletsBucket | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaOutletPage | Problem]
    """

    kwargs = _get_kwargs(
        q=q,
        country=country,
        type_=type_,
        bucket=bucket,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    country: str | Unset = UNSET,
    type_: ListMediaOutletsType | Unset = UNSET,
    bucket: ListMediaOutletsBucket | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> MediaOutletPage | Problem | None:
    """List media outlets

     Outlet registry with consensus per dimension.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str | Unset):
        country (str | Unset):
        type_ (ListMediaOutletsType | Unset):
        bucket (ListMediaOutletsBucket | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaOutletPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            q=q,
            country=country,
            type_=type_,
            bucket=bucket,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
