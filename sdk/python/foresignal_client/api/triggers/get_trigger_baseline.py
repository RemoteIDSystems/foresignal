from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.anomaly_baseline import AnomalyBaseline
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    rule_id: UUID,
    *,
    group_key: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["groupKey"] = group_key

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/triggers/{rule_id}/baseline".format(
            rule_id=quote(str(rule_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnomalyBaseline | Problem | None:
    if response.status_code == 200:
        response_200 = AnomalyBaseline.from_dict(response.json())

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
) -> Response[AnomalyBaseline | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    group_key: str | Unset = UNSET,
) -> Response[AnomalyBaseline | Problem]:
    """Anomaly rule baseline

     Per-slot n, mean and variance (and model) per group, plus weeks available. 409 BASELINE_NOT_READY
    before the first sample.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        group_key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnomalyBaseline | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        group_key=group_key,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    group_key: str | Unset = UNSET,
) -> AnomalyBaseline | Problem | None:
    """Anomaly rule baseline

     Per-slot n, mean and variance (and model) per group, plus weeks available. 409 BASELINE_NOT_READY
    before the first sample.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        group_key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnomalyBaseline | Problem
    """

    return sync_detailed(
        rule_id=rule_id,
        client=client,
        group_key=group_key,
    ).parsed


async def asyncio_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    group_key: str | Unset = UNSET,
) -> Response[AnomalyBaseline | Problem]:
    """Anomaly rule baseline

     Per-slot n, mean and variance (and model) per group, plus weeks available. 409 BASELINE_NOT_READY
    before the first sample.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        group_key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnomalyBaseline | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        group_key=group_key,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    group_key: str | Unset = UNSET,
) -> AnomalyBaseline | Problem | None:
    """Anomaly rule baseline

     Per-slot n, mean and variance (and model) per group, plus weeks available. 409 BASELINE_NOT_READY
    before the first sample.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        group_key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnomalyBaseline | Problem
    """

    return (
        await asyncio_detailed(
            rule_id=rule_id,
            client=client,
            group_key=group_key,
        )
    ).parsed
