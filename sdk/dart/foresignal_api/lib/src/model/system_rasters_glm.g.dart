// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_rasters_glm.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemRastersGlmCWProxy {
  SystemRastersGlm ringFlashes(int? ringFlashes);

  SystemRastersGlm archiveBytes(int? archiveBytes);

  SystemRastersGlm newestFileAt(DateTime? newestFileAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemRastersGlm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemRastersGlm(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemRastersGlm call({
    int? ringFlashes,
    int? archiveBytes,
    DateTime? newestFileAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemRastersGlm.copyWith(...)` or call `instanceOfSystemRastersGlm.copyWith.fieldName(value)` for a single field.
class _$SystemRastersGlmCWProxyImpl implements _$SystemRastersGlmCWProxy {
  const _$SystemRastersGlmCWProxyImpl(this._value);

  final SystemRastersGlm _value;

  @override
  SystemRastersGlm ringFlashes(int? ringFlashes) =>
      call(ringFlashes: ringFlashes);

  @override
  SystemRastersGlm archiveBytes(int? archiveBytes) =>
      call(archiveBytes: archiveBytes);

  @override
  SystemRastersGlm newestFileAt(DateTime? newestFileAt) =>
      call(newestFileAt: newestFileAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemRastersGlm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemRastersGlm(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemRastersGlm call({
    Object? ringFlashes = const $CopyWithPlaceholder(),
    Object? archiveBytes = const $CopyWithPlaceholder(),
    Object? newestFileAt = const $CopyWithPlaceholder(),
  }) {
    return SystemRastersGlm(
      ringFlashes: ringFlashes == const $CopyWithPlaceholder()
          ? _value.ringFlashes
          // ignore: cast_nullable_to_non_nullable
          : ringFlashes as int?,
      archiveBytes: archiveBytes == const $CopyWithPlaceholder()
          ? _value.archiveBytes
          // ignore: cast_nullable_to_non_nullable
          : archiveBytes as int?,
      newestFileAt: newestFileAt == const $CopyWithPlaceholder()
          ? _value.newestFileAt
          // ignore: cast_nullable_to_non_nullable
          : newestFileAt as DateTime?,
    );
  }
}

extension $SystemRastersGlmCopyWith on SystemRastersGlm {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemRastersGlm.copyWith(...)` or `instanceOfSystemRastersGlm.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemRastersGlmCWProxy get copyWith => _$SystemRastersGlmCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemRastersGlm _$SystemRastersGlmFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemRastersGlm', json, ($checkedConvert) {
  final val = SystemRastersGlm(
    ringFlashes: $checkedConvert('ringFlashes', (v) => (v as num?)?.toInt()),
    archiveBytes: $checkedConvert('archiveBytes', (v) => (v as num?)?.toInt()),
    newestFileAt: $checkedConvert(
      'newestFileAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$SystemRastersGlmToJson(SystemRastersGlm instance) =>
    <String, dynamic>{
      'ringFlashes': ?instance.ringFlashes,
      'archiveBytes': ?instance.archiveBytes,
      'newestFileAt': ?instance.newestFileAt?.toIso8601String(),
    };
