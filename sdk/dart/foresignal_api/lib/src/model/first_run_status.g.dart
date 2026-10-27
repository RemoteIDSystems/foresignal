// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'first_run_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FirstRunStatusCWProxy {
  FirstRunStatus firstRun(bool firstRun);

  FirstRunStatus hasUsers(bool hasUsers);

  FirstRunStatus smtpConfigured(bool? smtpConfigured);

  FirstRunStatus sourcesEnabled(int? sourcesEnabled);

  FirstRunStatus apiVersion(String? apiVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FirstRunStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FirstRunStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  FirstRunStatus call({
    bool firstRun,
    bool hasUsers,
    bool? smtpConfigured,
    int? sourcesEnabled,
    String? apiVersion,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFirstRunStatus.copyWith(...)` or call `instanceOfFirstRunStatus.copyWith.fieldName(value)` for a single field.
class _$FirstRunStatusCWProxyImpl implements _$FirstRunStatusCWProxy {
  const _$FirstRunStatusCWProxyImpl(this._value);

  final FirstRunStatus _value;

  @override
  FirstRunStatus firstRun(bool firstRun) => call(firstRun: firstRun);

  @override
  FirstRunStatus hasUsers(bool hasUsers) => call(hasUsers: hasUsers);

  @override
  FirstRunStatus smtpConfigured(bool? smtpConfigured) =>
      call(smtpConfigured: smtpConfigured);

  @override
  FirstRunStatus sourcesEnabled(int? sourcesEnabled) =>
      call(sourcesEnabled: sourcesEnabled);

  @override
  FirstRunStatus apiVersion(String? apiVersion) => call(apiVersion: apiVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FirstRunStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FirstRunStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FirstRunStatus call({
    Object? firstRun = const $CopyWithPlaceholder(),
    Object? hasUsers = const $CopyWithPlaceholder(),
    Object? smtpConfigured = const $CopyWithPlaceholder(),
    Object? sourcesEnabled = const $CopyWithPlaceholder(),
    Object? apiVersion = const $CopyWithPlaceholder(),
  }) {
    return FirstRunStatus(
      firstRun: firstRun == const $CopyWithPlaceholder() || firstRun == null
          ? _value.firstRun
          // ignore: cast_nullable_to_non_nullable
          : firstRun as bool,
      hasUsers: hasUsers == const $CopyWithPlaceholder() || hasUsers == null
          ? _value.hasUsers
          // ignore: cast_nullable_to_non_nullable
          : hasUsers as bool,
      smtpConfigured: smtpConfigured == const $CopyWithPlaceholder()
          ? _value.smtpConfigured
          // ignore: cast_nullable_to_non_nullable
          : smtpConfigured as bool?,
      sourcesEnabled: sourcesEnabled == const $CopyWithPlaceholder()
          ? _value.sourcesEnabled
          // ignore: cast_nullable_to_non_nullable
          : sourcesEnabled as int?,
      apiVersion: apiVersion == const $CopyWithPlaceholder()
          ? _value.apiVersion
          // ignore: cast_nullable_to_non_nullable
          : apiVersion as String?,
    );
  }
}

extension $FirstRunStatusCopyWith on FirstRunStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFirstRunStatus.copyWith(...)` or `instanceOfFirstRunStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FirstRunStatusCWProxy get copyWith => _$FirstRunStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FirstRunStatus _$FirstRunStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FirstRunStatus', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['firstRun', 'hasUsers']);
      final val = FirstRunStatus(
        firstRun: $checkedConvert('firstRun', (v) => v as bool),
        hasUsers: $checkedConvert('hasUsers', (v) => v as bool),
        smtpConfigured: $checkedConvert('smtpConfigured', (v) => v as bool?),
        sourcesEnabled: $checkedConvert(
          'sourcesEnabled',
          (v) => (v as num?)?.toInt(),
        ),
        apiVersion: $checkedConvert('apiVersion', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$FirstRunStatusToJson(FirstRunStatus instance) =>
    <String, dynamic>{
      'firstRun': instance.firstRun,
      'hasUsers': instance.hasUsers,
      'smtpConfigured': ?instance.smtpConfigured,
      'sourcesEnabled': ?instance.sourcesEnabled,
      'apiVersion': ?instance.apiVersion,
    };
