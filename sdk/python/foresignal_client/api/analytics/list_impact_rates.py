from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.impact_rate_page import ImpactRatePage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    hazard_class: str | Unset = UNSET,
    impact_family: str | Unset = UNSET,
    region_class: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["hazardClass"] = hazard_class

    params["impactFamily"] = impact_family

    params["regionClass"] = region_class

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analytics/impact-rates",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ImpactRatePage | Problem | None:
    if response.status_code == 200:
        response_200 = ImpactRatePage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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
) -> Response[ImpactRatePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    hazard_class: str | Unset = UNSET,
    impact_family: str | Unset = UNSET,
    region_class: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ImpactRatePage | Problem]:
    """Get conditional impact rates

     Historical rates of an impact family given a hazard class, with intervals and sample sizes; pairs
    with n < 20 are `insufficient`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        hazard_class (str | Unset):
        impact_family (str | Unset):
        region_class (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ImpactRatePage | Problem]
    """

    kwargs = _get_kwargs(
        hazard_class=hazard_class,
        impact_family=impact_family,
        region_class=region_class,
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
    hazard_class: str | Unset = UNSET,
    impact_family: str | Unset = UNSET,
    region_class: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ImpactRatePage | Problem | None:
    """Get conditional impact rates

     Historical rates of an impact family given a hazard class, with intervals and sample sizes; pairs
    with n < 20 are `insufficient`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        hazard_class (str | Unset):
        impact_family (str | Unset):
        region_class (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ImpactRatePage | Problem
    """

    return sync_detailed(
        client=client,
        hazard_class=hazard_class,
        impact_family=impact_family,
        region_class=region_class,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    hazard_class: str | Unset = UNSET,
    impact_family: str | Unset = UNSET,
    region_class: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ImpactRatePage | Problem]:
    """Get conditional impact rates

     Historical rates of an impact family given a hazard class, with intervals and sample sizes; pairs
    with n < 20 are `insufficient`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        hazard_class (str | Unset):
        impact_family (str | Unset):
        region_class (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ImpactRatePage | Problem]
    """

    kwargs = _get_kwargs(
        hazard_class=hazard_class,
        impact_family=impact_family,
        region_class=region_class,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    hazard_class: str | Unset = UNSET,
    impact_family: str | Unset = UNSET,
    region_class: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ImpactRatePage | Problem | None:
    """Get conditional impact rates

     Historical rates of an impact family given a hazard class, with intervals and sample sizes; pairs
    with n < 20 are `insufficient`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        hazard_class (str | Unset):
        impact_family (str | Unset):
        region_class (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ImpactRatePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            hazard_class=hazard_class,
            impact_family=impact_family,
            region_class=region_class,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
