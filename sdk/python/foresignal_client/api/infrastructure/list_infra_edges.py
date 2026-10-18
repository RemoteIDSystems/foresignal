from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.infra_edge_page import InfraEdgePage
from ...models.list_infra_edges_format import ListInfraEdgesFormat
from ...models.list_infra_edges_label_item import ListInfraEdgesLabelItem
from ...models.list_infra_edges_method_item import ListInfraEdgesMethodItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    bbox: str | Unset = UNSET,
    method: list[ListInfraEdgesMethodItem] | Unset = UNSET,
    label: list[ListInfraEdgesLabelItem] | Unset = UNSET,
    format_: ListInfraEdgesFormat | Unset = ListInfraEdgesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["bbox"] = bbox

    json_method: list[str] | Unset = UNSET
    if not isinstance(method, Unset):
        json_method = []
        for method_item_data in method:
            method_item = method_item_data.value
            json_method.append(method_item)

    params["method"] = json_method

    json_label: list[str] | Unset = UNSET
    if not isinstance(label, Unset):
        json_label = []
        for label_item_data in label:
            label_item = label_item_data.value
            json_label.append(label_item)

    params["label"] = json_label

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/infra/edges",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InfraEdgePage | Problem | None:
    if response.status_code == 200:
        response_200 = InfraEdgePage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[InfraEdgePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    method: list[ListInfraEdgesMethodItem] | Unset = UNSET,
    label: list[ListInfraEdgesLabelItem] | Unset = UNSET,
    format_: ListInfraEdgesFormat | Unset = ListInfraEdgesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[InfraEdgePage | Problem]:
    """List infrastructure edges

     Edges by area, method and label.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        method (list[ListInfraEdgesMethodItem] | Unset):
        label (list[ListInfraEdgesLabelItem] | Unset):
        format_ (ListInfraEdgesFormat | Unset):  Default: ListInfraEdgesFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraEdgePage | Problem]
    """

    kwargs = _get_kwargs(
        bbox=bbox,
        method=method,
        label=label,
        format_=format_,
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
    bbox: str | Unset = UNSET,
    method: list[ListInfraEdgesMethodItem] | Unset = UNSET,
    label: list[ListInfraEdgesLabelItem] | Unset = UNSET,
    format_: ListInfraEdgesFormat | Unset = ListInfraEdgesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> InfraEdgePage | Problem | None:
    """List infrastructure edges

     Edges by area, method and label.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        method (list[ListInfraEdgesMethodItem] | Unset):
        label (list[ListInfraEdgesLabelItem] | Unset):
        format_ (ListInfraEdgesFormat | Unset):  Default: ListInfraEdgesFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraEdgePage | Problem
    """

    return sync_detailed(
        client=client,
        bbox=bbox,
        method=method,
        label=label,
        format_=format_,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    method: list[ListInfraEdgesMethodItem] | Unset = UNSET,
    label: list[ListInfraEdgesLabelItem] | Unset = UNSET,
    format_: ListInfraEdgesFormat | Unset = ListInfraEdgesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[InfraEdgePage | Problem]:
    """List infrastructure edges

     Edges by area, method and label.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        method (list[ListInfraEdgesMethodItem] | Unset):
        label (list[ListInfraEdgesLabelItem] | Unset):
        format_ (ListInfraEdgesFormat | Unset):  Default: ListInfraEdgesFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraEdgePage | Problem]
    """

    kwargs = _get_kwargs(
        bbox=bbox,
        method=method,
        label=label,
        format_=format_,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    method: list[ListInfraEdgesMethodItem] | Unset = UNSET,
    label: list[ListInfraEdgesLabelItem] | Unset = UNSET,
    format_: ListInfraEdgesFormat | Unset = ListInfraEdgesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> InfraEdgePage | Problem | None:
    """List infrastructure edges

     Edges by area, method and label.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        method (list[ListInfraEdgesMethodItem] | Unset):
        label (list[ListInfraEdgesLabelItem] | Unset):
        format_ (ListInfraEdgesFormat | Unset):  Default: ListInfraEdgesFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraEdgePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            bbox=bbox,
            method=method,
            label=label,
            format_=format_,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
