import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast_scorecard import ForecastScorecard
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["forecaster"] = forecaster

    params["category"] = category

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
        "url": "/v1/forecasts/scorecard",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecastScorecard | Problem | None:
    if response.status_code == 200:
        response_200 = ForecastScorecard.from_dict(response.json())

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
) -> Response[ForecastScorecard | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> Response[ForecastScorecard | Problem]:
    """Get the forecast scorecard

     Brier, log score, RPS, CRPS, time-averaged Brier, Murphy decomposition, skill against out-of-sample
    references and the bias monitor per forecaster and category; `insufficient` under 20 resolved
    questions.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        forecaster (str | Unset):
        category (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastScorecard | Problem]
    """

    kwargs = _get_kwargs(
        forecaster=forecaster,
        category=category,
        from_=from_,
        to=to,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> ForecastScorecard | Problem | None:
    """Get the forecast scorecard

     Brier, log score, RPS, CRPS, time-averaged Brier, Murphy decomposition, skill against out-of-sample
    references and the bias monitor per forecaster and category; `insufficient` under 20 resolved
    questions.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        forecaster (str | Unset):
        category (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastScorecard | Problem
    """

    return sync_detailed(
        client=client,
        forecaster=forecaster,
        category=category,
        from_=from_,
        to=to,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> Response[ForecastScorecard | Problem]:
    """Get the forecast scorecard

     Brier, log score, RPS, CRPS, time-averaged Brier, Murphy decomposition, skill against out-of-sample
    references and the bias monitor per forecaster and category; `insufficient` under 20 resolved
    questions.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        forecaster (str | Unset):
        category (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastScorecard | Problem]
    """

    kwargs = _get_kwargs(
        forecaster=forecaster,
        category=category,
        from_=from_,
        to=to,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
) -> ForecastScorecard | Problem | None:
    """Get the forecast scorecard

     Brier, log score, RPS, CRPS, time-averaged Brier, Murphy decomposition, skill against out-of-sample
    references and the bias monitor per forecaster and category; `insufficient` under 20 resolved
    questions.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        forecaster (str | Unset):
        category (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastScorecard | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            forecaster=forecaster,
            category=category,
            from_=from_,
            to=to,
        )
    ).parsed
