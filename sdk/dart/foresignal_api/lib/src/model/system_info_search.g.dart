// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_search.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoSearchCWProxy {
  SystemInfoSearch semanticEnabled(bool? semanticEnabled);

  SystemInfoSearch reason(String? reason);

  SystemInfoSearch documents(int? documents);

  SystemInfoSearch backlog(int? backlog);

  SystemInfoSearch activeModel(String? activeModel);

  SystemInfoSearch reindexState(SystemInfoSearchReindexStateEnum? reindexState);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoSearch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoSearch(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoSearch call({
    bool? semanticEnabled,
    String? reason,
    int? documents,
    int? backlog,
    String? activeModel,
    SystemInfoSearchReindexStateEnum? reindexState,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoSearch.copyWith(...)` or call `instanceOfSystemInfoSearch.copyWith.fieldName(value)` for a single field.
class _$SystemInfoSearchCWProxyImpl implements _$SystemInfoSearchCWProxy {
  const _$SystemInfoSearchCWProxyImpl(this._value);

  final SystemInfoSearch _value;

  @override
  SystemInfoSearch semanticEnabled(bool? semanticEnabled) =>
      call(semanticEnabled: semanticEnabled);

  @override
  SystemInfoSearch reason(String? reason) => call(reason: reason);

  @override
  SystemInfoSearch documents(int? documents) => call(documents: documents);

  @override
  SystemInfoSearch backlog(int? backlog) => call(backlog: backlog);

  @override
  SystemInfoSearch activeModel(String? activeModel) =>
      call(activeModel: activeModel);

  @override
  SystemInfoSearch reindexState(
    SystemInfoSearchReindexStateEnum? reindexState,
  ) => call(reindexState: reindexState);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoSearch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoSearch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoSearch call({
    Object? semanticEnabled = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? documents = const $CopyWithPlaceholder(),
    Object? backlog = const $CopyWithPlaceholder(),
    Object? activeModel = const $CopyWithPlaceholder(),
    Object? reindexState = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoSearch(
      semanticEnabled: semanticEnabled == const $CopyWithPlaceholder()
          ? _value.semanticEnabled
          // ignore: cast_nullable_to_non_nullable
          : semanticEnabled as bool?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      documents: documents == const $CopyWithPlaceholder()
          ? _value.documents
          // ignore: cast_nullable_to_non_nullable
          : documents as int?,
      backlog: backlog == const $CopyWithPlaceholder()
          ? _value.backlog
          // ignore: cast_nullable_to_non_nullable
          : backlog as int?,
      activeModel: activeModel == const $CopyWithPlaceholder()
          ? _value.activeModel
          // ignore: cast_nullable_to_non_nullable
          : activeModel as String?,
      reindexState: reindexState == const $CopyWithPlaceholder()
          ? _value.reindexState
          // ignore: cast_nullable_to_non_nullable
          : reindexState as SystemInfoSearchReindexStateEnum?,
    );
  }
}

extension $SystemInfoSearchCopyWith on SystemInfoSearch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoSearch.copyWith(...)` or `instanceOfSystemInfoSearch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoSearchCWProxy get copyWith => _$SystemInfoSearchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoSearch _$SystemInfoSearchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoSearch', json, ($checkedConvert) {
      final val = SystemInfoSearch(
        semanticEnabled: $checkedConvert('semanticEnabled', (v) => v as bool?),
        reason: $checkedConvert('reason', (v) => v as String?),
        documents: $checkedConvert('documents', (v) => (v as num?)?.toInt()),
        backlog: $checkedConvert('backlog', (v) => (v as num?)?.toInt()),
        activeModel: $checkedConvert('activeModel', (v) => v as String?),
        reindexState: $checkedConvert(
          'reindexState',
          (v) =>
              $enumDecodeNullable(_$SystemInfoSearchReindexStateEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoSearchToJson(SystemInfoSearch instance) =>
    <String, dynamic>{
      'semanticEnabled': ?instance.semanticEnabled,
      'reason': ?instance.reason,
      'documents': ?instance.documents,
      'backlog': ?instance.backlog,
      'activeModel': ?instance.activeModel,
      'reindexState':
          ?_$SystemInfoSearchReindexStateEnumEnumMap[instance.reindexState],
    };

const _$SystemInfoSearchReindexStateEnumEnumMap = {
  SystemInfoSearchReindexStateEnum.idle: 'idle',
  SystemInfoSearchReindexStateEnum.backfilling: 'backfilling',
  SystemInfoSearchReindexStateEnum.indexing: 'indexing',
  SystemInfoSearchReindexStateEnum.swapping: 'swapping',
};
