// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'glance_alerts_latest_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GlanceAlertsLatestInnerCWProxy {
  GlanceAlertsLatestInner id(int id);

  GlanceAlertsLatestInner severity(
    GlanceAlertsLatestInnerSeverityEnum severity,
  );

  GlanceAlertsLatestInner shortTitle(String? shortTitle);

  GlanceAlertsLatestInner firedAt(DateTime firedAt);

  GlanceAlertsLatestInner near(String? near);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceAlertsLatestInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceAlertsLatestInner(...).copyWith(id: 12, name: "My name")
  /// ```
  GlanceAlertsLatestInner call({
    int id,
    GlanceAlertsLatestInnerSeverityEnum severity,
    String? shortTitle,
    DateTime firedAt,
    String? near,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGlanceAlertsLatestInner.copyWith(...)` or call `instanceOfGlanceAlertsLatestInner.copyWith.fieldName(value)` for a single field.
class _$GlanceAlertsLatestInnerCWProxyImpl
    implements _$GlanceAlertsLatestInnerCWProxy {
  const _$GlanceAlertsLatestInnerCWProxyImpl(this._value);

  final GlanceAlertsLatestInner _value;

  @override
  GlanceAlertsLatestInner id(int id) => call(id: id);

  @override
  GlanceAlertsLatestInner severity(
    GlanceAlertsLatestInnerSeverityEnum severity,
  ) => call(severity: severity);

  @override
  GlanceAlertsLatestInner shortTitle(String? shortTitle) =>
      call(shortTitle: shortTitle);

  @override
  GlanceAlertsLatestInner firedAt(DateTime firedAt) => call(firedAt: firedAt);

  @override
  GlanceAlertsLatestInner near(String? near) => call(near: near);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceAlertsLatestInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceAlertsLatestInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GlanceAlertsLatestInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? shortTitle = const $CopyWithPlaceholder(),
    Object? firedAt = const $CopyWithPlaceholder(),
    Object? near = const $CopyWithPlaceholder(),
  }) {
    return GlanceAlertsLatestInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as GlanceAlertsLatestInnerSeverityEnum,
      shortTitle: shortTitle == const $CopyWithPlaceholder()
          ? _value.shortTitle
          // ignore: cast_nullable_to_non_nullable
          : shortTitle as String?,
      firedAt: firedAt == const $CopyWithPlaceholder() || firedAt == null
          ? _value.firedAt
          // ignore: cast_nullable_to_non_nullable
          : firedAt as DateTime,
      near: near == const $CopyWithPlaceholder()
          ? _value.near
          // ignore: cast_nullable_to_non_nullable
          : near as String?,
    );
  }
}

extension $GlanceAlertsLatestInnerCopyWith on GlanceAlertsLatestInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGlanceAlertsLatestInner.copyWith(...)` or `instanceOfGlanceAlertsLatestInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GlanceAlertsLatestInnerCWProxy get copyWith =>
      _$GlanceAlertsLatestInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GlanceAlertsLatestInner _$GlanceAlertsLatestInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('GlanceAlertsLatestInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'severity', 'firedAt']);
  final val = GlanceAlertsLatestInner(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$GlanceAlertsLatestInnerSeverityEnumEnumMap, v),
    ),
    shortTitle: $checkedConvert('shortTitle', (v) => v as String?),
    firedAt: $checkedConvert('firedAt', (v) => DateTime.parse(v as String)),
    near: $checkedConvert('near', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$GlanceAlertsLatestInnerToJson(
  GlanceAlertsLatestInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'severity': _$GlanceAlertsLatestInnerSeverityEnumEnumMap[instance.severity]!,
  'shortTitle': ?instance.shortTitle,
  'firedAt': instance.firedAt.toIso8601String(),
  'near': ?instance.near,
};

const _$GlanceAlertsLatestInnerSeverityEnumEnumMap = {
  GlanceAlertsLatestInnerSeverityEnum.info: 'info',
  GlanceAlertsLatestInnerSeverityEnum.warning: 'warning',
  GlanceAlertsLatestInnerSeverityEnum.critical: 'critical',
};
