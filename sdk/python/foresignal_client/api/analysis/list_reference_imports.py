from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_reference_imports_dataset import ListReferenceImportsDataset
from ...models.list_reference_imports_status import ListReferenceImportsStatus
from ...models.problem import Problem
from ...models.reference_import_page import ReferenceImportPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    dataset: ListReferenceImportsDataset | Unset = UNSET,
    status: ListReferenceImportsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_dataset: str | Unset = UNSET
    if not isinstance(dataset, Unset):
        json_dataset = dataset.value

    params["dataset"] = json_dataset

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/reference-imports",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ReferenceImportPage | None:
    if response.status_code == 200:
        response_200 = ReferenceImportPage.from_dict(response.json())

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
) -> Response[Problem | ReferenceImportPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    dataset: ListReferenceImportsDataset | Unset = UNSET,
    status: ListReferenceImportsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | ReferenceImportPage]:
    """List reference-history imports

     Lists imports newest first.

    **Scope:** `admin`.

    Args:
        dataset (ListReferenceImportsDataset | Unset):
        status (ListReferenceImportsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReferenceImportPage]
    """

    kwargs = _get_kwargs(
        dataset=dataset,
        status=status,
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
    dataset: ListReferenceImportsDataset | Unset = UNSET,
    status: ListReferenceImportsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | ReferenceImportPage | None:
    """List reference-history imports

     Lists imports newest first.

    **Scope:** `admin`.

    Args:
        dataset (ListReferenceImportsDataset | Unset):
        status (ListReferenceImportsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReferenceImportPage
    """

    return sync_detailed(
        client=client,
        dataset=dataset,
        status=status,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    dataset: ListReferenceImportsDataset | Unset = UNSET,
    status: ListReferenceImportsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | ReferenceImportPage]:
    """List reference-history imports

     Lists imports newest first.

    **Scope:** `admin`.

    Args:
        dataset (ListReferenceImportsDataset | Unset):
        status (ListReferenceImportsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReferenceImportPage]
    """

    kwargs = _get_kwargs(
        dataset=dataset,
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    dataset: ListReferenceImportsDataset | Unset = UNSET,
    status: ListReferenceImportsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | ReferenceImportPage | None:
    """List reference-history imports

     Lists imports newest first.

    **Scope:** `admin`.

    Args:
        dataset (ListReferenceImportsDataset | Unset):
        status (ListReferenceImportsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReferenceImportPage
    """

    return (
        await asyncio_detailed(
            client=client,
            dataset=dataset,
            status=status,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
