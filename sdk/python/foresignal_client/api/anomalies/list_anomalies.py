import datetime
from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.anomaly_observation_page import AnomalyObservationPage
from ...models.list_anomalies_classification_item import ListAnomaliesClassificationItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    rule_id: UUID | Unset = UNSET,
    group_key: str | Unset = UNSET,
    classification: list[ListAnomaliesClassificationItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_rule_id: str | Unset = UNSET
    if not isinstance(rule_id, Unset):
        json_rule_id = str(rule_id)
    params["ruleId"] = json_rule_id

    params["groupKey"] = group_key

    json_classification: list[str] | Unset = UNSET
    if not isinstance(classification, Unset):
        json_classification = []
        for classification_item_data in classification:
            classification_item = classification_item_data.value
            json_classification.append(classification_item)

    params["classification"] = json_classification

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

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/anomalies",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnomalyObservationPage | Problem | None:
    if response.status_code == 200:
        response_200 = AnomalyObservationPage.from_dict(response.json())

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
) -> Response[AnomalyObservationPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    rule_id: UUID | Unset = UNSET,
    group_key: str | Unset = UNSET,
    classification: list[ListAnomaliesClassificationItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AnomalyObservationPage | Problem]:
    """List anomaly results

     Evaluated anomaly buckets, newest first (keyset on id). p-values are descriptive under the stated
    count model.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID | Unset):
        group_key (str | Unset):
        classification (list[ListAnomaliesClassificationItem] | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnomalyObservationPage | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        group_key=group_key,
        classification=classification,
        since=since,
        until=until,
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
    rule_id: UUID | Unset = UNSET,
    group_key: str | Unset = UNSET,
    classification: list[ListAnomaliesClassificationItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AnomalyObservationPage | Problem | None:
    """List anomaly results

     Evaluated anomaly buckets, newest first (keyset on id). p-values are descriptive under the stated
    count model.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID | Unset):
        group_key (str | Unset):
        classification (list[ListAnomaliesClassificationItem] | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnomalyObservationPage | Problem
    """

    return sync_detailed(
        client=client,
        rule_id=rule_id,
        group_key=group_key,
        classification=classification,
        since=since,
        until=until,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    rule_id: UUID | Unset = UNSET,
    group_key: str | Unset = UNSET,
    classification: list[ListAnomaliesClassificationItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AnomalyObservationPage | Problem]:
    """List anomaly results

     Evaluated anomaly buckets, newest first (keyset on id). p-values are descriptive under the stated
    count model.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID | Unset):
        group_key (str | Unset):
        classification (list[ListAnomaliesClassificationItem] | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnomalyObservationPage | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        group_key=group_key,
        classification=classification,
        since=since,
        until=until,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    rule_id: UUID | Unset = UNSET,
    group_key: str | Unset = UNSET,
    classification: list[ListAnomaliesClassificationItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AnomalyObservationPage | Problem | None:
    """List anomaly results

     Evaluated anomaly buckets, newest first (keyset on id). p-values are descriptive under the stated
    count model.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID | Unset):
        group_key (str | Unset):
        classification (list[ListAnomaliesClassificationItem] | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnomalyObservationPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            rule_id=rule_id,
            group_key=group_key,
            classification=classification,
            since=since,
            until=until,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
