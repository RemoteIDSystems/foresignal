import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.regional_index_detail import RegionalIndexDetail
from ...types import UNSET, Response, Unset


def _get_kwargs(
    index_id: str,
    *,
    region: str | Unset = UNSET,
    from_: datetime.date | Unset = UNSET,
    to: datetime.date | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["region"] = region

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/indices/{index_id}".format(
            index_id=quote(str(index_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RegionalIndexDetail | None:
    if response.status_code == 200:
        response_200 = RegionalIndexDetail.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | RegionalIndexDetail]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    index_id: str,
    *,
    client: AuthenticatedClient,
    region: str | Unset = UNSET,
    from_: datetime.date | Unset = UNSET,
    to: datetime.date | Unset = UNSET,
) -> Response[Problem | RegionalIndexDetail]:
    """Get a regional index with values

     The definition, skill, weights origin and daily values with their decomposition; without `region`,
    the latest value per region.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        index_id (str):  Example: instability.
        region (str | Unset):
        from_ (datetime.date | Unset):
        to (datetime.date | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RegionalIndexDetail]
    """

    kwargs = _get_kwargs(
        index_id=index_id,
        region=region,
        from_=from_,
        to=to,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    index_id: str,
    *,
    client: AuthenticatedClient,
    region: str | Unset = UNSET,
    from_: datetime.date | Unset = UNSET,
    to: datetime.date | Unset = UNSET,
) -> Problem | RegionalIndexDetail | None:
    """Get a regional index with values

     The definition, skill, weights origin and daily values with their decomposition; without `region`,
    the latest value per region.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        index_id (str):  Example: instability.
        region (str | Unset):
        from_ (datetime.date | Unset):
        to (datetime.date | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RegionalIndexDetail
    """

    return sync_detailed(
        index_id=index_id,
        client=client,
        region=region,
        from_=from_,
        to=to,
    ).parsed


async def asyncio_detailed(
    index_id: str,
    *,
    client: AuthenticatedClient,
    region: str | Unset = UNSET,
    from_: datetime.date | Unset = UNSET,
    to: datetime.date | Unset = UNSET,
) -> Response[Problem | RegionalIndexDetail]:
    """Get a regional index with values

     The definition, skill, weights origin and daily values with their decomposition; without `region`,
    the latest value per region.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        index_id (str):  Example: instability.
        region (str | Unset):
        from_ (datetime.date | Unset):
        to (datetime.date | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RegionalIndexDetail]
    """

    kwargs = _get_kwargs(
        index_id=index_id,
        region=region,
        from_=from_,
        to=to,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    index_id: str,
    *,
    client: AuthenticatedClient,
    region: str | Unset = UNSET,
    from_: datetime.date | Unset = UNSET,
    to: datetime.date | Unset = UNSET,
) -> Problem | RegionalIndexDetail | None:
    """Get a regional index with values

     The definition, skill, weights origin and daily values with their decomposition; without `region`,
    the latest value per region.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        index_id (str):  Example: instability.
        region (str | Unset):
        from_ (datetime.date | Unset):
        to (datetime.date | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RegionalIndexDetail
    """

    return (
        await asyncio_detailed(
            index_id=index_id,
            client=client,
            region=region,
            from_=from_,
            to=to,
        )
    ).parsed
