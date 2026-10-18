// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_submitted_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastSubmittedEventCWProxy {
  ForecastSubmittedEvent event(ForecastSubmittedEventEventEnum event);

  ForecastSubmittedEvent questionId(String questionId);

  ForecastSubmittedEvent forecastId(String forecastId);

  ForecastSubmittedEvent forecasterId(String forecasterId);

  ForecastSubmittedEvent madeAt(DateTime madeAt);

  ForecastSubmittedEvent probability(num? probability);

  ForecastSubmittedEvent estimativeTerm(IcdTerm? estimativeTerm);

  ForecastSubmittedEvent private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastSubmittedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastSubmittedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastSubmittedEvent call({
    ForecastSubmittedEventEventEnum event,
    String questionId,
    String forecastId,
    String forecasterId,
    DateTime madeAt,
    num? probability,
    IcdTerm? estimativeTerm,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastSubmittedEvent.copyWith(...)` or call `instanceOfForecastSubmittedEvent.copyWith.fieldName(value)` for a single field.
class _$ForecastSubmittedEventCWProxyImpl
    implements _$ForecastSubmittedEventCWProxy {
  const _$ForecastSubmittedEventCWProxyImpl(this._value);

  final ForecastSubmittedEvent _value;

  @override
  ForecastSubmittedEvent event(ForecastSubmittedEventEventEnum event) =>
      call(event: event);

  @override
  ForecastSubmittedEvent questionId(String questionId) =>
      call(questionId: questionId);

  @override
  ForecastSubmittedEvent forecastId(String forecastId) =>
      call(forecastId: forecastId);

  @override
  ForecastSubmittedEvent forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ForecastSubmittedEvent madeAt(DateTime madeAt) => call(madeAt: madeAt);

  @override
  ForecastSubmittedEvent probability(num? probability) =>
      call(probability: probability);

  @override
  ForecastSubmittedEvent estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  ForecastSubmittedEvent private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastSubmittedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastSubmittedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastSubmittedEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? forecastId = const $CopyWithPlaceholder(),
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? madeAt = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return ForecastSubmittedEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as ForecastSubmittedEventEventEnum,
      questionId:
          questionId == const $CopyWithPlaceholder() || questionId == null
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String,
      forecastId:
          forecastId == const $CopyWithPlaceholder() || forecastId == null
          ? _value.forecastId
          // ignore: cast_nullable_to_non_nullable
          : forecastId as String,
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      madeAt: madeAt == const $CopyWithPlaceholder() || madeAt == null
          ? _value.madeAt
          // ignore: cast_nullable_to_non_nullable
          : madeAt as DateTime,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $ForecastSubmittedEventCopyWith on ForecastSubmittedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastSubmittedEvent.copyWith(...)` or `instanceOfForecastSubmittedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastSubmittedEventCWProxy get copyWith =>
      _$ForecastSubmittedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastSubmittedEvent _$ForecastSubmittedEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastSubmittedEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'event',
      'questionId',
      'forecastId',
      'forecasterId',
      'madeAt',
    ],
  );
  final val = ForecastSubmittedEvent(
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$ForecastSubmittedEventEventEnumEnumMap, v),
    ),
    questionId: $checkedConvert('questionId', (v) => v as String),
    forecastId: $checkedConvert('forecastId', (v) => v as String),
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    madeAt: $checkedConvert('madeAt', (v) => DateTime.parse(v as String)),
    probability: $checkedConvert('probability', (v) => v as num?),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
    ),
    private: $checkedConvert('private', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ForecastSubmittedEventToJson(
  ForecastSubmittedEvent instance,
) => <String, dynamic>{
  'event': _$ForecastSubmittedEventEventEnumEnumMap[instance.event]!,
  'questionId': instance.questionId,
  'forecastId': instance.forecastId,
  'forecasterId': instance.forecasterId,
  'madeAt': instance.madeAt.toIso8601String(),
  'probability': ?instance.probability,
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
  'private': ?instance.private,
};

const _$ForecastSubmittedEventEventEnumEnumMap = {
  ForecastSubmittedEventEventEnum.forecastPeriodSubmitted: 'forecast.submitted',
};

const _$IcdTermEnumMap = {
  IcdTerm.almostNoChance: 'almost no chance',
  IcdTerm.veryUnlikely: 'very unlikely',
  IcdTerm.unlikely: 'unlikely',
  IcdTerm.roughlyEvenChance: 'roughly even chance',
  IcdTerm.likely: 'likely',
  IcdTerm.veryLikely: 'very likely',
  IcdTerm.almostCertain: 'almost certain',
};
