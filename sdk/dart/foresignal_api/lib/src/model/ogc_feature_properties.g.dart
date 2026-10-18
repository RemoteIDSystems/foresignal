// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_feature_properties.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcFeaturePropertiesCWProxy {
  OgcFeatureProperties title(String? title);

  OgcFeatureProperties sourceId(String? sourceId);

  OgcFeatureProperties observedAt(DateTime? observedAt);

  OgcFeatureProperties eventTime(DateTime? eventTime);

  OgcFeatureProperties version(int? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcFeatureProperties(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcFeatureProperties(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcFeatureProperties call({
    String? title,
    String? sourceId,
    DateTime? observedAt,
    DateTime? eventTime,
    int? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcFeatureProperties.copyWith(...)` or call `instanceOfOgcFeatureProperties.copyWith.fieldName(value)` for a single field.
class _$OgcFeaturePropertiesCWProxyImpl
    implements _$OgcFeaturePropertiesCWProxy {
  const _$OgcFeaturePropertiesCWProxyImpl(this._value);

  final OgcFeatureProperties _value;

  @override
  OgcFeatureProperties title(String? title) => call(title: title);

  @override
  OgcFeatureProperties sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  OgcFeatureProperties observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  OgcFeatureProperties eventTime(DateTime? eventTime) =>
      call(eventTime: eventTime);

  @override
  OgcFeatureProperties version(int? version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcFeatureProperties(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcFeatureProperties(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcFeatureProperties call({
    Object? title = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return OgcFeatureProperties(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
    );
  }
}

extension $OgcFeaturePropertiesCopyWith on OgcFeatureProperties {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcFeatureProperties.copyWith(...)` or `instanceOfOgcFeatureProperties.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcFeaturePropertiesCWProxy get copyWith =>
      _$OgcFeaturePropertiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcFeatureProperties _$OgcFeaturePropertiesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcFeatureProperties', json, ($checkedConvert) {
  final val = OgcFeatureProperties(
    title: $checkedConvert('title', (v) => v as String?),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    eventTime: $checkedConvert(
      'eventTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$OgcFeaturePropertiesToJson(
  OgcFeatureProperties instance,
) => <String, dynamic>{
  'title': ?instance.title,
  'sourceId': ?instance.sourceId,
  'observedAt': ?instance.observedAt?.toIso8601String(),
  'eventTime': ?instance.eventTime?.toIso8601String(),
  'version': ?instance.version,
};
