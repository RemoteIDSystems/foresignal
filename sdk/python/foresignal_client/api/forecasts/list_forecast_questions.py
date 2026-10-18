import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast_question_page import ForecastQuestionPage
from ...models.list_forecast_questions_resolver_item import (
    ListForecastQuestionsResolverItem,
)
from ...models.list_forecast_questions_status_item import (
    ListForecastQuestionsStatusItem,
)
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: list[ListForecastQuestionsStatusItem] | Unset = UNSET,
    category: list[str] | Unset = UNSET,
    linked: str | Unset = UNSET,
    resolver: list[ListForecastQuestionsResolverItem] | Unset = UNSET,
    due_before: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: list[str] | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = []
        for status_item_data in status:
            status_item = status_item_data.value
            json_status.append(status_item)

    params["status"] = json_status

    json_category: list[str] | Unset = UNSET
    if not isinstance(category, Unset):
        json_category = category

    params["category"] = json_category

    params["linked"] = linked

    json_resolver: list[str] | Unset = UNSET
    if not isinstance(resolver, Unset):
        json_resolver = []
        for resolver_item_data in resolver:
            resolver_item = resolver_item_data.value
            json_resolver.append(resolver_item)

    params["resolver"] = json_resolver

    json_due_before: str | Unset = UNSET
    if not isinstance(due_before, Unset):
        json_due_before = due_before.isoformat()
    params["dueBefore"] = json_due_before

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/forecasts/questions",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecastQuestionPage | Problem | None:
    if response.status_code == 200:
        response_200 = ForecastQuestionPage.from_dict(response.json())

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
) -> Response[ForecastQuestionPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: list[ListForecastQuestionsStatusItem] | Unset = UNSET,
    category: list[str] | Unset = UNSET,
    linked: str | Unset = UNSET,
    resolver: list[ListForecastQuestionsResolverItem] | Unset = UNSET,
    due_before: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ForecastQuestionPage | Problem]:
    """List forecast questions

     Lists questions with their latest ensemble probability and ICD term. Manual questions awaiting
    resolution: `status=closed&resolver=manual`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListForecastQuestionsStatusItem] | Unset):
        category (list[str] | Unset):
        linked (str | Unset):
        resolver (list[ListForecastQuestionsResolverItem] | Unset):
        due_before (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastQuestionPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        category=category,
        linked=linked,
        resolver=resolver,
        due_before=due_before,
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
    status: list[ListForecastQuestionsStatusItem] | Unset = UNSET,
    category: list[str] | Unset = UNSET,
    linked: str | Unset = UNSET,
    resolver: list[ListForecastQuestionsResolverItem] | Unset = UNSET,
    due_before: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ForecastQuestionPage | Problem | None:
    """List forecast questions

     Lists questions with their latest ensemble probability and ICD term. Manual questions awaiting
    resolution: `status=closed&resolver=manual`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListForecastQuestionsStatusItem] | Unset):
        category (list[str] | Unset):
        linked (str | Unset):
        resolver (list[ListForecastQuestionsResolverItem] | Unset):
        due_before (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastQuestionPage | Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        category=category,
        linked=linked,
        resolver=resolver,
        due_before=due_before,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: list[ListForecastQuestionsStatusItem] | Unset = UNSET,
    category: list[str] | Unset = UNSET,
    linked: str | Unset = UNSET,
    resolver: list[ListForecastQuestionsResolverItem] | Unset = UNSET,
    due_before: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ForecastQuestionPage | Problem]:
    """List forecast questions

     Lists questions with their latest ensemble probability and ICD term. Manual questions awaiting
    resolution: `status=closed&resolver=manual`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListForecastQuestionsStatusItem] | Unset):
        category (list[str] | Unset):
        linked (str | Unset):
        resolver (list[ListForecastQuestionsResolverItem] | Unset):
        due_before (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastQuestionPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        category=category,
        linked=linked,
        resolver=resolver,
        due_before=due_before,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: list[ListForecastQuestionsStatusItem] | Unset = UNSET,
    category: list[str] | Unset = UNSET,
    linked: str | Unset = UNSET,
    resolver: list[ListForecastQuestionsResolverItem] | Unset = UNSET,
    due_before: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ForecastQuestionPage | Problem | None:
    """List forecast questions

     Lists questions with their latest ensemble probability and ICD term. Manual questions awaiting
    resolution: `status=closed&resolver=manual`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListForecastQuestionsStatusItem] | Unset):
        category (list[str] | Unset):
        linked (str | Unset):
        resolver (list[ListForecastQuestionsResolverItem] | Unset):
        due_before (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastQuestionPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            category=category,
            linked=linked,
            resolver=resolver,
            due_before=due_before,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
