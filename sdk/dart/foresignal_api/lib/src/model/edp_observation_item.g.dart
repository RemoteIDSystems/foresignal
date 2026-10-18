// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_observation_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpObservationItemCWProxy {
  EdpObservationItem stream(String stream);

  EdpObservationItem type(EdpObservationItemTypeEnum type);

  EdpObservationItem id(String id);

  EdpObservationItem observedAt(DateTime? observedAt);

  EdpObservationItem properties(Map<String, Object>? properties);

  EdpObservationItem seriesKey(String seriesKey);

  EdpObservationItem t(String t);

  EdpObservationItem value(num? value);

  EdpObservationItem knownAt(DateTime? knownAt);

  EdpObservationItem unit(String? unit);

  EdpObservationItem origin(EdpOrigin? origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpObservationItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpObservationItem(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpObservationItem call({
    String stream,
    EdpObservationItemTypeEnum type,
    String id,
    DateTime? observedAt,
    Map<String, Object>? properties,
    String seriesKey,
    String t,
    num? value,
    DateTime? knownAt,
    String? unit,
    EdpOrigin? origin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpObservationItem.copyWith(...)` or call `instanceOfEdpObservationItem.copyWith.fieldName(value)` for a single field.
class _$EdpObservationItemCWProxyImpl implements _$EdpObservationItemCWProxy {
  const _$EdpObservationItemCWProxyImpl(this._value);

  final EdpObservationItem _value;

  @override
  EdpObservationItem stream(String stream) => call(stream: stream);

  @override
  EdpObservationItem type(EdpObservationItemTypeEnum type) => call(type: type);

  @override
  EdpObservationItem id(String id) => call(id: id);

  @override
  EdpObservationItem observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  EdpObservationItem properties(Map<String, Object>? properties) =>
      call(properties: properties);

  @override
  EdpObservationItem seriesKey(String seriesKey) => call(seriesKey: seriesKey);

  @override
  EdpObservationItem t(String t) => call(t: t);

  @override
  EdpObservationItem value(num? value) => call(value: value);

  @override
  EdpObservationItem knownAt(DateTime? knownAt) => call(knownAt: knownAt);

  @override
  EdpObservationItem unit(String? unit) => call(unit: unit);

  @override
  EdpObservationItem origin(EdpOrigin? origin) => call(origin: origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpObservationItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpObservationItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpObservationItem call({
    Object? stream = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? seriesKey = const $CopyWithPlaceholder(),
    Object? t = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? knownAt = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
  }) {
    return EdpObservationItem(
      stream: stream == const $CopyWithPlaceholder() || stream == null
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EdpObservationItemTypeEnum,
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
      seriesKey: seriesKey == const $CopyWithPlaceholder() || seriesKey == null
          ? _value.seriesKey
          // ignore: cast_nullable_to_non_nullable
          : seriesKey as String,
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as String,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      knownAt: knownAt == const $CopyWithPlaceholder()
          ? _value.knownAt
          // ignore: cast_nullable_to_non_nullable
          : knownAt as DateTime?,
      unit: unit == const $CopyWithPlaceholder()
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as EdpOrigin?,
    );
  }
}

extension $EdpObservationItemCopyWith on EdpObservationItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpObservationItem.copyWith(...)` or `instanceOfEdpObservationItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpObservationItemCWProxy get copyWith =>
      _$EdpObservationItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpObservationItem _$EdpObservationItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpObservationItem', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['stream', 'type', 'id', 'seriesKey', 't', 'value'],
      );
      final val = EdpObservationItem(
        stream: $checkedConvert('stream', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$EdpObservationItemTypeEnumEnumMap, v),
        ),
        id: $checkedConvert('id', (v) => v as String),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        properties: $checkedConvert(
          'properties',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        seriesKey: $checkedConvert('seriesKey', (v) => v as String),
        t: $checkedConvert('t', (v) => v as String),
        value: $checkedConvert('value', (v) => v as num?),
        knownAt: $checkedConvert(
          'knownAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        unit: $checkedConvert('unit', (v) => v as String?),
        origin: $checkedConvert(
          'origin',
          (v) =>
              v == null ? null : EdpOrigin.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpObservationItemToJson(EdpObservationItem instance) =>
    <String, dynamic>{
      'stream': instance.stream,
      'type': _$EdpObservationItemTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'properties': ?instance.properties,
      'seriesKey': instance.seriesKey,
      't': instance.t,
      'value': instance.value,
      'knownAt': ?instance.knownAt?.toIso8601String(),
      'unit': ?instance.unit,
      'origin': ?instance.origin?.toJson(),
    };

const _$EdpObservationItemTypeEnumEnumMap = {
  EdpObservationItemTypeEnum.observation: 'observation',
};
