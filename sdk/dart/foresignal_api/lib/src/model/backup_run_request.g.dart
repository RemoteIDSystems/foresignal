// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backup_run_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BackupRunRequestCWProxy {
  BackupRunRequest type(String? type);

  BackupRunRequest label(String? label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupRunRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupRunRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  BackupRunRequest call({String? type, String? label});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBackupRunRequest.copyWith(...)` or call `instanceOfBackupRunRequest.copyWith.fieldName(value)` for a single field.
class _$BackupRunRequestCWProxyImpl implements _$BackupRunRequestCWProxy {
  const _$BackupRunRequestCWProxyImpl(this._value);

  final BackupRunRequest _value;

  @override
  BackupRunRequest type(String? type) => call(type: type);

  @override
  BackupRunRequest label(String? label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupRunRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupRunRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BackupRunRequest call({
    Object? type = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return BackupRunRequest(
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
    );
  }
}

extension $BackupRunRequestCopyWith on BackupRunRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBackupRunRequest.copyWith(...)` or `instanceOfBackupRunRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BackupRunRequestCWProxy get copyWith => _$BackupRunRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BackupRunRequest _$BackupRunRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BackupRunRequest', json, ($checkedConvert) {
      final val = BackupRunRequest(
        type: $checkedConvert('type', (v) => v as String?),
        label: $checkedConvert('label', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BackupRunRequestToJson(BackupRunRequest instance) =>
    <String, dynamic>{'type': ?instance.type, 'label': ?instance.label};
