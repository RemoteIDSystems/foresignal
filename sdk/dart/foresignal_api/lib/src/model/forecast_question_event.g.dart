// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_question_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastQuestionEventCWProxy {
  ForecastQuestionEvent event(ForecastQuestionEventEventEnum event);

  ForecastQuestionEvent question(ForecastQuestionSummary question);

  ForecastQuestionEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastQuestionEvent call({
    ForecastQuestionEventEventEnum event,
    ForecastQuestionSummary question,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastQuestionEvent.copyWith(...)` or call `instanceOfForecastQuestionEvent.copyWith.fieldName(value)` for a single field.
class _$ForecastQuestionEventCWProxyImpl
    implements _$ForecastQuestionEventCWProxy {
  const _$ForecastQuestionEventCWProxyImpl(this._value);

  final ForecastQuestionEvent _value;

  @override
  ForecastQuestionEvent event(ForecastQuestionEventEventEnum event) =>
      call(event: event);

  @override
  ForecastQuestionEvent question(ForecastQuestionSummary question) =>
      call(question: question);

  @override
  ForecastQuestionEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastQuestionEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? question = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ForecastQuestionEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as ForecastQuestionEventEventEnum,
      question: question == const $CopyWithPlaceholder() || question == null
          ? _value.question
          // ignore: cast_nullable_to_non_nullable
          : question as ForecastQuestionSummary,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ForecastQuestionEventCopyWith on ForecastQuestionEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastQuestionEvent.copyWith(...)` or `instanceOfForecastQuestionEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastQuestionEventCWProxy get copyWith =>
      _$ForecastQuestionEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastQuestionEvent _$ForecastQuestionEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastQuestionEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['event', 'question', 'at']);
  final val = ForecastQuestionEvent(
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$ForecastQuestionEventEventEnumEnumMap, v),
    ),
    question: $checkedConvert(
      'question',
      (v) => ForecastQuestionSummary.fromJson(v as Map<String, dynamic>),
    ),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ForecastQuestionEventToJson(
  ForecastQuestionEvent instance,
) => <String, dynamic>{
  'event': _$ForecastQuestionEventEventEnumEnumMap[instance.event]!,
  'question': instance.question.toJson(),
  'at': instance.at.toIso8601String(),
};

const _$ForecastQuestionEventEventEnumEnumMap = {
  ForecastQuestionEventEventEnum.questionPeriodOpened: 'question.opened',
  ForecastQuestionEventEventEnum.questionPeriodClosed: 'question.closed',
  ForecastQuestionEventEventEnum.questionPeriodResolved: 'question.resolved',
  ForecastQuestionEventEventEnum.questionPeriodAnnulled: 'question.annulled',
};
