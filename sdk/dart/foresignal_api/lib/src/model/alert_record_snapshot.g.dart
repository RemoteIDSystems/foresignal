// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_record_snapshot.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertRecordSnapshotCWProxy {
  AlertRecordSnapshot kind(String kind);

  AlertRecordSnapshot version(int version);

  AlertRecordSnapshot observedAt(DateTime observedAt);

  AlertRecordSnapshot geometry(Geometry? geometry);

  AlertRecordSnapshot payload(Map<String, Object>? payload);

  AlertRecordSnapshot truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertRecordSnapshot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertRecordSnapshot(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertRecordSnapshot call({
    String kind,
    int version,
    DateTime observedAt,
    Geometry? geometry,
    Map<String, Object>? payload,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertRecordSnapshot.copyWith(...)` or call `instanceOfAlertRecordSnapshot.copyWith.fieldName(value)` for a single field.
class _$AlertRecordSnapshotCWProxyImpl implements _$AlertRecordSnapshotCWProxy {
  const _$AlertRecordSnapshotCWProxyImpl(this._value);

  final AlertRecordSnapshot _value;

  @override
  AlertRecordSnapshot kind(String kind) => call(kind: kind);

  @override
  AlertRecordSnapshot version(int version) => call(version: version);

  @override
  AlertRecordSnapshot observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  AlertRecordSnapshot geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  AlertRecordSnapshot payload(Map<String, Object>? payload) =>
      call(payload: payload);

  @override
  AlertRecordSnapshot truncated(bool? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertRecordSnapshot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertRecordSnapshot(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertRecordSnapshot call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return AlertRecordSnapshot(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      payload: payload == const $CopyWithPlaceholder()
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $AlertRecordSnapshotCopyWith on AlertRecordSnapshot {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertRecordSnapshot.copyWith(...)` or `instanceOfAlertRecordSnapshot.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertRecordSnapshotCWProxy get copyWith =>
      _$AlertRecordSnapshotCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertRecordSnapshot _$AlertRecordSnapshotFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AlertRecordSnapshot', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['kind', 'version', 'observedAt']);
      final val = AlertRecordSnapshot(
        kind: $checkedConvert('kind', (v) => v as String),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        payload: $checkedConvert(
          'payload',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        truncated: $checkedConvert('truncated', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$AlertRecordSnapshotToJson(
  AlertRecordSnapshot instance,
) => <String, dynamic>{
  'kind': instance.kind,
  'version': instance.version,
  'observedAt': instance.observedAt.toIso8601String(),
  'geometry': ?instance.geometry?.toJson(),
  'payload': ?instance.payload,
  'truncated': ?instance.truncated,
};
