// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_database.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoDatabaseCWProxy {
  SystemInfoDatabase status(SystemInfoDatabaseStatusEnum? status);

  SystemInfoDatabase sizeBytes(int? sizeBytes);

  SystemInfoDatabase postgisVersion(String? postgisVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoDatabase(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoDatabase(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoDatabase call({
    SystemInfoDatabaseStatusEnum? status,
    int? sizeBytes,
    String? postgisVersion,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoDatabase.copyWith(...)` or call `instanceOfSystemInfoDatabase.copyWith.fieldName(value)` for a single field.
class _$SystemInfoDatabaseCWProxyImpl implements _$SystemInfoDatabaseCWProxy {
  const _$SystemInfoDatabaseCWProxyImpl(this._value);

  final SystemInfoDatabase _value;

  @override
  SystemInfoDatabase status(SystemInfoDatabaseStatusEnum? status) =>
      call(status: status);

  @override
  SystemInfoDatabase sizeBytes(int? sizeBytes) => call(sizeBytes: sizeBytes);

  @override
  SystemInfoDatabase postgisVersion(String? postgisVersion) =>
      call(postgisVersion: postgisVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoDatabase(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoDatabase(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoDatabase call({
    Object? status = const $CopyWithPlaceholder(),
    Object? sizeBytes = const $CopyWithPlaceholder(),
    Object? postgisVersion = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoDatabase(
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SystemInfoDatabaseStatusEnum?,
      sizeBytes: sizeBytes == const $CopyWithPlaceholder()
          ? _value.sizeBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeBytes as int?,
      postgisVersion: postgisVersion == const $CopyWithPlaceholder()
          ? _value.postgisVersion
          // ignore: cast_nullable_to_non_nullable
          : postgisVersion as String?,
    );
  }
}

extension $SystemInfoDatabaseCopyWith on SystemInfoDatabase {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoDatabase.copyWith(...)` or `instanceOfSystemInfoDatabase.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoDatabaseCWProxy get copyWith =>
      _$SystemInfoDatabaseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoDatabase _$SystemInfoDatabaseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoDatabase', json, ($checkedConvert) {
      final val = SystemInfoDatabase(
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SystemInfoDatabaseStatusEnumEnumMap, v),
        ),
        sizeBytes: $checkedConvert('sizeBytes', (v) => (v as num?)?.toInt()),
        postgisVersion: $checkedConvert('postgisVersion', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoDatabaseToJson(SystemInfoDatabase instance) =>
    <String, dynamic>{
      'status': ?_$SystemInfoDatabaseStatusEnumEnumMap[instance.status],
      'sizeBytes': ?instance.sizeBytes,
      'postgisVersion': ?instance.postgisVersion,
    };

const _$SystemInfoDatabaseStatusEnumEnumMap = {
  SystemInfoDatabaseStatusEnum.up: 'up',
  SystemInfoDatabaseStatusEnum.down: 'down',
};
