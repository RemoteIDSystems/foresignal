// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_archive.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleArchiveCWProxy {
  ChronicleArchive state(ChronicleArchiveStateEnum state);

  ChronicleArchive exportId(String? exportId);

  ChronicleArchive archivedAt(DateTime? archivedAt);

  ChronicleArchive bytes(int? bytes);

  ChronicleArchive error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleArchive(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleArchive(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleArchive call({
    ChronicleArchiveStateEnum state,
    String? exportId,
    DateTime? archivedAt,
    int? bytes,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleArchive.copyWith(...)` or call `instanceOfChronicleArchive.copyWith.fieldName(value)` for a single field.
class _$ChronicleArchiveCWProxyImpl implements _$ChronicleArchiveCWProxy {
  const _$ChronicleArchiveCWProxyImpl(this._value);

  final ChronicleArchive _value;

  @override
  ChronicleArchive state(ChronicleArchiveStateEnum state) => call(state: state);

  @override
  ChronicleArchive exportId(String? exportId) => call(exportId: exportId);

  @override
  ChronicleArchive archivedAt(DateTime? archivedAt) =>
      call(archivedAt: archivedAt);

  @override
  ChronicleArchive bytes(int? bytes) => call(bytes: bytes);

  @override
  ChronicleArchive error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleArchive(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleArchive(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleArchive call({
    Object? state = const $CopyWithPlaceholder(),
    Object? exportId = const $CopyWithPlaceholder(),
    Object? archivedAt = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return ChronicleArchive(
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as ChronicleArchiveStateEnum,
      exportId: exportId == const $CopyWithPlaceholder()
          ? _value.exportId
          // ignore: cast_nullable_to_non_nullable
          : exportId as String?,
      archivedAt: archivedAt == const $CopyWithPlaceholder()
          ? _value.archivedAt
          // ignore: cast_nullable_to_non_nullable
          : archivedAt as DateTime?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $ChronicleArchiveCopyWith on ChronicleArchive {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleArchive.copyWith(...)` or `instanceOfChronicleArchive.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleArchiveCWProxy get copyWith => _$ChronicleArchiveCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleArchive _$ChronicleArchiveFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleArchive', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['state']);
      final val = ChronicleArchive(
        state: $checkedConvert(
          'state',
          (v) => $enumDecode(_$ChronicleArchiveStateEnumEnumMap, v),
        ),
        exportId: $checkedConvert('exportId', (v) => v as String?),
        archivedAt: $checkedConvert(
          'archivedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleArchiveToJson(ChronicleArchive instance) =>
    <String, dynamic>{
      'state': _$ChronicleArchiveStateEnumEnumMap[instance.state]!,
      'exportId': ?instance.exportId,
      'archivedAt': ?instance.archivedAt?.toIso8601String(),
      'bytes': ?instance.bytes,
      'error': ?instance.error,
    };

const _$ChronicleArchiveStateEnumEnumMap = {
  ChronicleArchiveStateEnum.pending: 'pending',
  ChronicleArchiveStateEnum.archived: 'archived',
  ChronicleArchiveStateEnum.restoring: 'restoring',
  ChronicleArchiveStateEnum.failed: 'failed',
};
