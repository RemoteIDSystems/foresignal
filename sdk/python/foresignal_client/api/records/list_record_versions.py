import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_record_versions_include_item import ListRecordVersionsIncludeItem
from ...models.list_record_versions_licence_policy_item import (
    ListRecordVersionsLicencePolicyItem,
)
from ...models.problem import Problem
from ...models.record_version_page import RecordVersionPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    external_id: str,
    *,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    include: list[ListRecordVersionsIncludeItem] | Unset = UNSET,
    licence_policy: list[ListRecordVersionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    json_until: str | Unset = UNSET
    if not isinstance(until, Unset):
        json_until = until.isoformat()
    params["until"] = json_until

    params["cursor"] = cursor

    params["limit"] = limit

    json_include: list[str] | Unset = UNSET
    if not isinstance(include, Unset):
        json_include = []
        for include_item_data in include:
            include_item = include_item_data.value
            json_include.append(include_item)

    params["include"] = json_include

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/records/{source_id}/{external_id}/versions".format(
            source_id=quote(str(source_id), safe=""),
            external_id=quote(str(external_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RecordVersionPage | None:
    if response.status_code == 200:
        response_200 = RecordVersionPage.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | RecordVersionPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    include: list[ListRecordVersionsIncludeItem] | Unset = UNSET,
    licence_policy: list[ListRecordVersionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Problem | RecordVersionPage]:
    """Version history of a record

     Versions of one record ordered by seq, each with persistReason, ingestedAt, receivedAt, batchId,
    quality and (gated) observer.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        include (list[ListRecordVersionsIncludeItem] | Unset):
        licence_policy (list[ListRecordVersionsLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RecordVersionPage]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        since=since,
        until=until,
        cursor=cursor,
        limit=limit,
        include=include,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    include: list[ListRecordVersionsIncludeItem] | Unset = UNSET,
    licence_policy: list[ListRecordVersionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Problem | RecordVersionPage | None:
    """Version history of a record

     Versions of one record ordered by seq, each with persistReason, ingestedAt, receivedAt, batchId,
    quality and (gated) observer.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        include (list[ListRecordVersionsIncludeItem] | Unset):
        licence_policy (list[ListRecordVersionsLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RecordVersionPage
    """

    return sync_detailed(
        source_id=source_id,
        external_id=external_id,
        client=client,
        since=since,
        until=until,
        cursor=cursor,
        limit=limit,
        include=include,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    include: list[ListRecordVersionsIncludeItem] | Unset = UNSET,
    licence_policy: list[ListRecordVersionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Problem | RecordVersionPage]:
    """Version history of a record

     Versions of one record ordered by seq, each with persistReason, ingestedAt, receivedAt, batchId,
    quality and (gated) observer.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        include (list[ListRecordVersionsIncludeItem] | Unset):
        licence_policy (list[ListRecordVersionsLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RecordVersionPage]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        since=since,
        until=until,
        cursor=cursor,
        limit=limit,
        include=include,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    include: list[ListRecordVersionsIncludeItem] | Unset = UNSET,
    licence_policy: list[ListRecordVersionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Problem | RecordVersionPage | None:
    """Version history of a record

     Versions of one record ordered by seq, each with persistReason, ingestedAt, receivedAt, batchId,
    quality and (gated) observer.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        include (list[ListRecordVersionsIncludeItem] | Unset):
        licence_policy (list[ListRecordVersionsLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RecordVersionPage
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            external_id=external_id,
            client=client,
            since=since,
            until=until,
            cursor=cursor,
            limit=limit,
            include=include,
            licence_policy=licence_policy,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
