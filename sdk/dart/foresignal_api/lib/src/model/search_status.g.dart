// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'search_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SearchStatusCWProxy {
  SearchStatus semanticEnabled(bool semanticEnabled);

  SearchStatus reason(String? reason);

  SearchStatus model(String? model);

  SearchStatus dimensions(int? dimensions);

  SearchStatus documents(int documents);

  SearchStatus backlog(int? backlog);

  SearchStatus reindexing(bool? reindexing);

  SearchStatus activeModel(String? activeModel);

  SearchStatus reindex(SearchReindexState? reindex);

  SearchStatus degraded(bool? degraded);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SearchStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SearchStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  SearchStatus call({
    bool semanticEnabled,
    String? reason,
    String? model,
    int? dimensions,
    int documents,
    int? backlog,
    bool? reindexing,
    String? activeModel,
    SearchReindexState? reindex,
    bool? degraded,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSearchStatus.copyWith(...)` or call `instanceOfSearchStatus.copyWith.fieldName(value)` for a single field.
class _$SearchStatusCWProxyImpl implements _$SearchStatusCWProxy {
  const _$SearchStatusCWProxyImpl(this._value);

  final SearchStatus _value;

  @override
  SearchStatus semanticEnabled(bool semanticEnabled) =>
      call(semanticEnabled: semanticEnabled);

  @override
  SearchStatus reason(String? reason) => call(reason: reason);

  @override
  SearchStatus model(String? model) => call(model: model);

  @override
  SearchStatus dimensions(int? dimensions) => call(dimensions: dimensions);

  @override
  SearchStatus documents(int documents) => call(documents: documents);

  @override
  SearchStatus backlog(int? backlog) => call(backlog: backlog);

  @override
  SearchStatus reindexing(bool? reindexing) => call(reindexing: reindexing);

  @override
  SearchStatus activeModel(String? activeModel) =>
      call(activeModel: activeModel);

  @override
  SearchStatus reindex(SearchReindexState? reindex) => call(reindex: reindex);

  @override
  SearchStatus degraded(bool? degraded) => call(degraded: degraded);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SearchStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SearchStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SearchStatus call({
    Object? semanticEnabled = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? dimensions = const $CopyWithPlaceholder(),
    Object? documents = const $CopyWithPlaceholder(),
    Object? backlog = const $CopyWithPlaceholder(),
    Object? reindexing = const $CopyWithPlaceholder(),
    Object? activeModel = const $CopyWithPlaceholder(),
    Object? reindex = const $CopyWithPlaceholder(),
    Object? degraded = const $CopyWithPlaceholder(),
  }) {
    return SearchStatus(
      semanticEnabled:
          semanticEnabled == const $CopyWithPlaceholder() ||
              semanticEnabled == null
          ? _value.semanticEnabled
          // ignore: cast_nullable_to_non_nullable
          : semanticEnabled as bool,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      dimensions: dimensions == const $CopyWithPlaceholder()
          ? _value.dimensions
          // ignore: cast_nullable_to_non_nullable
          : dimensions as int?,
      documents: documents == const $CopyWithPlaceholder() || documents == null
          ? _value.documents
          // ignore: cast_nullable_to_non_nullable
          : documents as int,
      backlog: backlog == const $CopyWithPlaceholder()
          ? _value.backlog
          // ignore: cast_nullable_to_non_nullable
          : backlog as int?,
      reindexing: reindexing == const $CopyWithPlaceholder()
          ? _value.reindexing
          // ignore: cast_nullable_to_non_nullable
          : reindexing as bool?,
      activeModel: activeModel == const $CopyWithPlaceholder()
          ? _value.activeModel
          // ignore: cast_nullable_to_non_nullable
          : activeModel as String?,
      reindex: reindex == const $CopyWithPlaceholder()
          ? _value.reindex
          // ignore: cast_nullable_to_non_nullable
          : reindex as SearchReindexState?,
      degraded: degraded == const $CopyWithPlaceholder()
          ? _value.degraded
          // ignore: cast_nullable_to_non_nullable
          : degraded as bool?,
    );
  }
}

extension $SearchStatusCopyWith on SearchStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSearchStatus.copyWith(...)` or `instanceOfSearchStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SearchStatusCWProxy get copyWith => _$SearchStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SearchStatus _$SearchStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SearchStatus', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['semanticEnabled', 'documents']);
      final val = SearchStatus(
        semanticEnabled: $checkedConvert('semanticEnabled', (v) => v as bool),
        reason: $checkedConvert('reason', (v) => v as String?),
        model: $checkedConvert('model', (v) => v as String?),
        dimensions: $checkedConvert('dimensions', (v) => (v as num?)?.toInt()),
        documents: $checkedConvert('documents', (v) => (v as num).toInt()),
        backlog: $checkedConvert('backlog', (v) => (v as num?)?.toInt()),
        reindexing: $checkedConvert('reindexing', (v) => v as bool?),
        activeModel: $checkedConvert('activeModel', (v) => v as String?),
        reindex: $checkedConvert(
          'reindex',
          (v) => v == null
              ? null
              : SearchReindexState.fromJson(v as Map<String, dynamic>),
        ),
        degraded: $checkedConvert('degraded', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$SearchStatusToJson(SearchStatus instance) =>
    <String, dynamic>{
      'semanticEnabled': instance.semanticEnabled,
      'reason': ?instance.reason,
      'model': ?instance.model,
      'dimensions': ?instance.dimensions,
      'documents': instance.documents,
      'backlog': ?instance.backlog,
      'reindexing': ?instance.reindexing,
      'activeModel': ?instance.activeModel,
      'reindex': ?instance.reindex?.toJson(),
      'degraded': ?instance.degraded,
    };
