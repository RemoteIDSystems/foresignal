// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resource_status_database.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResourceStatusDatabaseCWProxy {
  ResourceStatusDatabase sizeBytes(int? sizeBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceStatusDatabase(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceStatusDatabase(...).copyWith(id: 12, name: "My name")
  /// ```
  ResourceStatusDatabase call({int? sizeBytes});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResourceStatusDatabase.copyWith(...)` or call `instanceOfResourceStatusDatabase.copyWith.fieldName(value)` for a single field.
class _$ResourceStatusDatabaseCWProxyImpl
    implements _$ResourceStatusDatabaseCWProxy {
  const _$ResourceStatusDatabaseCWProxyImpl(this._value);

  final ResourceStatusDatabase _value;

  @override
  ResourceStatusDatabase sizeBytes(int? sizeBytes) =>
      call(sizeBytes: sizeBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceStatusDatabase(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceStatusDatabase(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResourceStatusDatabase call({
    Object? sizeBytes = const $CopyWithPlaceholder(),
  }) {
    return ResourceStatusDatabase(
      sizeBytes: sizeBytes == const $CopyWithPlaceholder()
          ? _value.sizeBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeBytes as int?,
    );
  }
}

extension $ResourceStatusDatabaseCopyWith on ResourceStatusDatabase {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResourceStatusDatabase.copyWith(...)` or `instanceOfResourceStatusDatabase.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResourceStatusDatabaseCWProxy get copyWith =>
      _$ResourceStatusDatabaseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResourceStatusDatabase _$ResourceStatusDatabaseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ResourceStatusDatabase', json, ($checkedConvert) {
  final val = ResourceStatusDatabase(
    sizeBytes: $checkedConvert('sizeBytes', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ResourceStatusDatabaseToJson(
  ResourceStatusDatabase instance,
) => <String, dynamic>{'sizeBytes': ?instance.sizeBytes};
