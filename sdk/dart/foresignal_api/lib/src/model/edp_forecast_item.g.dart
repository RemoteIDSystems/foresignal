// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_forecast_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpForecastItemCWProxy {
  EdpForecastItem stream(String stream);

  EdpForecastItem type(EdpForecastItemTypeEnum type);

  EdpForecastItem id(String id);

  EdpForecastItem observedAt(DateTime? observedAt);

  EdpForecastItem properties(Map<String, Object>? properties);

  EdpForecastItem question(EdpForecastItemAllOfQuestion? question);

  EdpForecastItem questionRef(String? questionRef);

  EdpForecastItem probability(num? probability);

  EdpForecastItem quantiles(Quantiles? quantiles);

  EdpForecastItem madeAt(DateTime madeAt);

  EdpForecastItem origin(EdpOrigin? origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpForecastItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpForecastItem(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpForecastItem call({
    String stream,
    EdpForecastItemTypeEnum type,
    String id,
    DateTime? observedAt,
    Map<String, Object>? properties,
    EdpForecastItemAllOfQuestion? question,
    String? questionRef,
    num? probability,
    Quantiles? quantiles,
    DateTime madeAt,
    EdpOrigin? origin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpForecastItem.copyWith(...)` or call `instanceOfEdpForecastItem.copyWith.fieldName(value)` for a single field.
class _$EdpForecastItemCWProxyImpl implements _$EdpForecastItemCWProxy {
  const _$EdpForecastItemCWProxyImpl(this._value);

  final EdpForecastItem _value;

  @override
  EdpForecastItem stream(String stream) => call(stream: stream);

  @override
  EdpForecastItem type(EdpForecastItemTypeEnum type) => call(type: type);

  @override
  EdpForecastItem id(String id) => call(id: id);

  @override
  EdpForecastItem observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  EdpForecastItem properties(Map<String, Object>? properties) =>
      call(properties: properties);

  @override
  EdpForecastItem question(EdpForecastItemAllOfQuestion? question) =>
      call(question: question);

  @override
  EdpForecastItem questionRef(String? questionRef) =>
      call(questionRef: questionRef);

  @override
  EdpForecastItem probability(num? probability) =>
      call(probability: probability);

  @override
  EdpForecastItem quantiles(Quantiles? quantiles) => call(quantiles: quantiles);

  @override
  EdpForecastItem madeAt(DateTime madeAt) => call(madeAt: madeAt);

  @override
  EdpForecastItem origin(EdpOrigin? origin) => call(origin: origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpForecastItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpForecastItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpForecastItem call({
    Object? stream = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? question = const $CopyWithPlaceholder(),
    Object? questionRef = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? quantiles = const $CopyWithPlaceholder(),
    Object? madeAt = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
  }) {
    return EdpForecastItem(
      stream: stream == const $CopyWithPlaceholder() || stream == null
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EdpForecastItemTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      properties: properties == const $CopyWithPlaceholder()
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as Map<String, Object>?,
      question: question == const $CopyWithPlaceholder()
          ? _value.question
          // ignore: cast_nullable_to_non_nullable
          : question as EdpForecastItemAllOfQuestion?,
      questionRef: questionRef == const $CopyWithPlaceholder()
          ? _value.questionRef
          // ignore: cast_nullable_to_non_nullable
          : questionRef as String?,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      quantiles: quantiles == const $CopyWithPlaceholder()
          ? _value.quantiles
          // ignore: cast_nullable_to_non_nullable
          : quantiles as Quantiles?,
      madeAt: madeAt == const $CopyWithPlaceholder() || madeAt == null
          ? _value.madeAt
          // ignore: cast_nullable_to_non_nullable
          : madeAt as DateTime,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as EdpOrigin?,
    );
  }
}

extension $EdpForecastItemCopyWith on EdpForecastItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpForecastItem.copyWith(...)` or `instanceOfEdpForecastItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpForecastItemCWProxy get copyWith => _$EdpForecastItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpForecastItem _$EdpForecastItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpForecastItem', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['stream', 'type', 'id', 'madeAt']);
  final val = EdpForecastItem(
    stream: $checkedConvert('stream', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$EdpForecastItemTypeEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    properties: $checkedConvert(
      'properties',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    question: $checkedConvert(
      'question',
      (v) => v == null
          ? null
          : EdpForecastItemAllOfQuestion.fromJson(v as Map<String, dynamic>),
    ),
    questionRef: $checkedConvert('questionRef', (v) => v as String?),
    probability: $checkedConvert('probability', (v) => v as num?),
    quantiles: $checkedConvert(
      'quantiles',
      (v) => v == null ? null : Quantiles.fromJson(v as Map<String, dynamic>),
    ),
    madeAt: $checkedConvert('madeAt', (v) => DateTime.parse(v as String)),
    origin: $checkedConvert(
      'origin',
      (v) => v == null ? null : EdpOrigin.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$EdpForecastItemToJson(EdpForecastItem instance) =>
    <String, dynamic>{
      'stream': instance.stream,
      'type': _$EdpForecastItemTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'properties': ?instance.properties,
      'question': ?instance.question?.toJson(),
      'questionRef': ?instance.questionRef,
      'probability': ?instance.probability,
      'quantiles': ?instance.quantiles?.toJson(),
      'madeAt': instance.madeAt.toIso8601String(),
      'origin': ?instance.origin?.toJson(),
    };

const _$EdpForecastItemTypeEnumEnumMap = {
  EdpForecastItemTypeEnum.forecast: 'forecast',
};
