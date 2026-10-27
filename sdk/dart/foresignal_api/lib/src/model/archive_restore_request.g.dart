// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_restore_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchiveRestoreRequestCWProxy {
  ArchiveRestoreRequest confirm(bool confirm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveRestoreRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveRestoreRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchiveRestoreRequest call({bool confirm});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchiveRestoreRequest.copyWith(...)` or call `instanceOfArchiveRestoreRequest.copyWith.fieldName(value)` for a single field.
class _$ArchiveRestoreRequestCWProxyImpl
    implements _$ArchiveRestoreRequestCWProxy {
  const _$ArchiveRestoreRequestCWProxyImpl(this._value);

  final ArchiveRestoreRequest _value;

  @override
  ArchiveRestoreRequest confirm(bool confirm) => call(confirm: confirm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveRestoreRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveRestoreRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchiveRestoreRequest call({Object? confirm = const $CopyWithPlaceholder()}) {
    return ArchiveRestoreRequest(
      confirm: confirm == const $CopyWithPlaceholder() || confirm == null
          ? _value.confirm
          // ignore: cast_nullable_to_non_nullable
          : confirm as bool,
    );
  }
}

extension $ArchiveRestoreRequestCopyWith on ArchiveRestoreRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchiveRestoreRequest.copyWith(...)` or `instanceOfArchiveRestoreRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchiveRestoreRequestCWProxy get copyWith =>
      _$ArchiveRestoreRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchiveRestoreRequest _$ArchiveRestoreRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ArchiveRestoreRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['confirm']);
  final val = ArchiveRestoreRequest(
    confirm: $checkedConvert('confirm', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$ArchiveRestoreRequestToJson(
  ArchiveRestoreRequest instance,
) => <String, dynamic>{'confirm': instance.confirm};
