// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_question_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalQuestionLinkCWProxy {
  ExternalQuestionLink streamId(String streamId);

  ExternalQuestionLink externalQuestionId(String externalQuestionId);

  ExternalQuestionLink questionId(String questionId);

  ExternalQuestionLink orientation(
    ExternalQuestionLinkOrientationEnum? orientation,
  );

  ExternalQuestionLink id(String id);

  ExternalQuestionLink sourceId(String sourceId);

  ExternalQuestionLink forecasterId(String forecasterId);

  ExternalQuestionLink createdAt(DateTime createdAt);

  ExternalQuestionLink lastForecastAt(DateTime? lastForecastAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalQuestionLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalQuestionLink(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalQuestionLink call({
    String streamId,
    String externalQuestionId,
    String questionId,
    ExternalQuestionLinkOrientationEnum? orientation,
    String id,
    String sourceId,
    String forecasterId,
    DateTime createdAt,
    DateTime? lastForecastAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalQuestionLink.copyWith(...)` or call `instanceOfExternalQuestionLink.copyWith.fieldName(value)` for a single field.
class _$ExternalQuestionLinkCWProxyImpl
    implements _$ExternalQuestionLinkCWProxy {
  const _$ExternalQuestionLinkCWProxyImpl(this._value);

  final ExternalQuestionLink _value;

  @override
  ExternalQuestionLink streamId(String streamId) => call(streamId: streamId);

  @override
  ExternalQuestionLink externalQuestionId(String externalQuestionId) =>
      call(externalQuestionId: externalQuestionId);

  @override
  ExternalQuestionLink questionId(String questionId) =>
      call(questionId: questionId);

  @override
  ExternalQuestionLink orientation(
    ExternalQuestionLinkOrientationEnum? orientation,
  ) => call(orientation: orientation);

  @override
  ExternalQuestionLink id(String id) => call(id: id);

  @override
  ExternalQuestionLink sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ExternalQuestionLink forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ExternalQuestionLink createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  ExternalQuestionLink lastForecastAt(DateTime? lastForecastAt) =>
      call(lastForecastAt: lastForecastAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalQuestionLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalQuestionLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalQuestionLink call({
    Object? streamId = const $CopyWithPlaceholder(),
    Object? externalQuestionId = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? orientation = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastForecastAt = const $CopyWithPlaceholder(),
  }) {
    return ExternalQuestionLink(
      streamId: streamId == const $CopyWithPlaceholder() || streamId == null
          ? _value.streamId
          // ignore: cast_nullable_to_non_nullable
          : streamId as String,
      externalQuestionId:
          externalQuestionId == const $CopyWithPlaceholder() ||
              externalQuestionId == null
          ? _value.externalQuestionId
          // ignore: cast_nullable_to_non_nullable
          : externalQuestionId as String,
      questionId:
          questionId == const $CopyWithPlaceholder() || questionId == null
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String,
      orientation: orientation == const $CopyWithPlaceholder()
          ? _value.orientation
          // ignore: cast_nullable_to_non_nullable
          : orientation as ExternalQuestionLinkOrientationEnum?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      lastForecastAt: lastForecastAt == const $CopyWithPlaceholder()
          ? _value.lastForecastAt
          // ignore: cast_nullable_to_non_nullable
          : lastForecastAt as DateTime?,
    );
  }
}

extension $ExternalQuestionLinkCopyWith on ExternalQuestionLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalQuestionLink.copyWith(...)` or `instanceOfExternalQuestionLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalQuestionLinkCWProxy get copyWith =>
      _$ExternalQuestionLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalQuestionLink _$ExternalQuestionLinkFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalQuestionLink', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'streamId',
      'externalQuestionId',
      'questionId',
      'id',
      'sourceId',
      'forecasterId',
      'createdAt',
    ],
  );
  final val = ExternalQuestionLink(
    streamId: $checkedConvert('streamId', (v) => v as String),
    externalQuestionId: $checkedConvert(
      'externalQuestionId',
      (v) => v as String,
    ),
    questionId: $checkedConvert('questionId', (v) => v as String),
    orientation: $checkedConvert(
      'orientation',
      (v) =>
          $enumDecodeNullable(_$ExternalQuestionLinkOrientationEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    lastForecastAt: $checkedConvert(
      'lastForecastAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExternalQuestionLinkToJson(
  ExternalQuestionLink instance,
) => <String, dynamic>{
  'streamId': instance.streamId,
  'externalQuestionId': instance.externalQuestionId,
  'questionId': instance.questionId,
  'orientation':
      ?_$ExternalQuestionLinkOrientationEnumEnumMap[instance.orientation],
  'id': instance.id,
  'sourceId': instance.sourceId,
  'forecasterId': instance.forecasterId,
  'createdAt': instance.createdAt.toIso8601String(),
  'lastForecastAt': ?instance.lastForecastAt?.toIso8601String(),
};

const _$ExternalQuestionLinkOrientationEnumEnumMap = {
  ExternalQuestionLinkOrientationEnum.same: 'same',
  ExternalQuestionLinkOrientationEnum.inverse: 'inverse',
};
