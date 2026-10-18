// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_report_fixture.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorReportFixtureCWProxy {
  DoctorReportFixture file(String file);

  DoctorReportFixture capturedAt(String? capturedAt);

  DoctorReportFixture sha256(String? sha256);

  DoctorReportFixture truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportFixture(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportFixture(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorReportFixture call({
    String file,
    String? capturedAt,
    String? sha256,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorReportFixture.copyWith(...)` or call `instanceOfDoctorReportFixture.copyWith.fieldName(value)` for a single field.
class _$DoctorReportFixtureCWProxyImpl implements _$DoctorReportFixtureCWProxy {
  const _$DoctorReportFixtureCWProxyImpl(this._value);

  final DoctorReportFixture _value;

  @override
  DoctorReportFixture file(String file) => call(file: file);

  @override
  DoctorReportFixture capturedAt(String? capturedAt) =>
      call(capturedAt: capturedAt);

  @override
  DoctorReportFixture sha256(String? sha256) => call(sha256: sha256);

  @override
  DoctorReportFixture truncated(bool? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportFixture(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportFixture(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorReportFixture call({
    Object? file = const $CopyWithPlaceholder(),
    Object? capturedAt = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return DoctorReportFixture(
      file: file == const $CopyWithPlaceholder() || file == null
          ? _value.file
          // ignore: cast_nullable_to_non_nullable
          : file as String,
      capturedAt: capturedAt == const $CopyWithPlaceholder()
          ? _value.capturedAt
          // ignore: cast_nullable_to_non_nullable
          : capturedAt as String?,
      sha256: sha256 == const $CopyWithPlaceholder()
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $DoctorReportFixtureCopyWith on DoctorReportFixture {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorReportFixture.copyWith(...)` or `instanceOfDoctorReportFixture.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorReportFixtureCWProxy get copyWith =>
      _$DoctorReportFixtureCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorReportFixture _$DoctorReportFixtureFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DoctorReportFixture', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['file']);
      final val = DoctorReportFixture(
        file: $checkedConvert('file', (v) => v as String),
        capturedAt: $checkedConvert('capturedAt', (v) => v as String?),
        sha256: $checkedConvert('sha256', (v) => v as String?),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$DoctorReportFixtureToJson(
  DoctorReportFixture instance,
) => <String, dynamic>{
  'file': instance.file,
  'capturedAt': ?instance.capturedAt,
  'sha256': ?instance.sha256,
  'truncated': ?instance.truncated,
};
