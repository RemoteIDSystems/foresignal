// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'search_reindex_state.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SearchReindexStateCWProxy {
  SearchReindexState state(SearchReindexStateStateEnum state);

  SearchReindexState fromModel(String? fromModel);

  SearchReindexState toModel(String? toModel);

  SearchReindexState total(int? total);

  SearchReindexState done(int? done);

  SearchReindexState percent(num? percent);

  SearchReindexState ratePerMinute(num? ratePerMinute);

  SearchReindexState etaSeconds(int? etaSeconds);

  SearchReindexState startedAt(DateTime? startedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SearchReindexState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SearchReindexState(...).copyWith(id: 12, name: "My name")
  /// ```
  SearchReindexState call({
    SearchReindexStateStateEnum state,
    String? fromModel,
    String? toModel,
    int? total,
    int? done,
    num? percent,
    num? ratePerMinute,
    int? etaSeconds,
    DateTime? startedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSearchReindexState.copyWith(...)` or call `instanceOfSearchReindexState.copyWith.fieldName(value)` for a single field.
class _$SearchReindexStateCWProxyImpl implements _$SearchReindexStateCWProxy {
  const _$SearchReindexStateCWProxyImpl(this._value);

  final SearchReindexState _value;

  @override
  SearchReindexState state(SearchReindexStateStateEnum state) =>
      call(state: state);

  @override
  SearchReindexState fromModel(String? fromModel) => call(fromModel: fromModel);

  @override
  SearchReindexState toModel(String? toModel) => call(toModel: toModel);

  @override
  SearchReindexState total(int? total) => call(total: total);

  @override
  SearchReindexState done(int? done) => call(done: done);

  @override
  SearchReindexState percent(num? percent) => call(percent: percent);

  @override
  SearchReindexState ratePerMinute(num? ratePerMinute) =>
      call(ratePerMinute: ratePerMinute);

  @override
  SearchReindexState etaSeconds(int? etaSeconds) =>
      call(etaSeconds: etaSeconds);

  @override
  SearchReindexState startedAt(DateTime? startedAt) =>
      call(startedAt: startedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SearchReindexState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SearchReindexState(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SearchReindexState call({
    Object? state = const $CopyWithPlaceholder(),
    Object? fromModel = const $CopyWithPlaceholder(),
    Object? toModel = const $CopyWithPlaceholder(),
    Object? total = const $CopyWithPlaceholder(),
    Object? done = const $CopyWithPlaceholder(),
    Object? percent = const $CopyWithPlaceholder(),
    Object? ratePerMinute = const $CopyWithPlaceholder(),
    Object? etaSeconds = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
  }) {
    return SearchReindexState(
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as SearchReindexStateStateEnum,
      fromModel: fromModel == const $CopyWithPlaceholder()
          ? _value.fromModel
          // ignore: cast_nullable_to_non_nullable
          : fromModel as String?,
      toModel: toModel == const $CopyWithPlaceholder()
          ? _value.toModel
          // ignore: cast_nullable_to_non_nullable
          : toModel as String?,
      total: total == const $CopyWithPlaceholder()
          ? _value.total
          // ignore: cast_nullable_to_non_nullable
          : total as int?,
      done: done == const $CopyWithPlaceholder()
          ? _value.done
          // ignore: cast_nullable_to_non_nullable
          : done as int?,
      percent: percent == const $CopyWithPlaceholder()
          ? _value.percent
          // ignore: cast_nullable_to_non_nullable
          : percent as num?,
      ratePerMinute: ratePerMinute == const $CopyWithPlaceholder()
          ? _value.ratePerMinute
          // ignore: cast_nullable_to_non_nullable
          : ratePerMinute as num?,
      etaSeconds: etaSeconds == const $CopyWithPlaceholder()
          ? _value.etaSeconds
          // ignore: cast_nullable_to_non_nullable
          : etaSeconds as int?,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
    );
  }
}

extension $SearchReindexStateCopyWith on SearchReindexState {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSearchReindexState.copyWith(...)` or `instanceOfSearchReindexState.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SearchReindexStateCWProxy get copyWith =>
      _$SearchReindexStateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SearchReindexState _$SearchReindexStateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SearchReindexState', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['state']);
      final val = SearchReindexState(
        state: $checkedConvert(
          'state',
          (v) => $enumDecode(_$SearchReindexStateStateEnumEnumMap, v),
        ),
        fromModel: $checkedConvert('fromModel', (v) => v as String?),
        toModel: $checkedConvert('toModel', (v) => v as String?),
        total: $checkedConvert('total', (v) => (v as num?)?.toInt()),
        done: $checkedConvert('done', (v) => (v as num?)?.toInt()),
        percent: $checkedConvert('percent', (v) => v as num?),
        ratePerMinute: $checkedConvert('ratePerMinute', (v) => v as num?),
        etaSeconds: $checkedConvert('etaSeconds', (v) => (v as num?)?.toInt()),
        startedAt: $checkedConvert(
          'startedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SearchReindexStateToJson(SearchReindexState instance) =>
    <String, dynamic>{
      'state': _$SearchReindexStateStateEnumEnumMap[instance.state]!,
      'fromModel': ?instance.fromModel,
      'toModel': ?instance.toModel,
      'total': ?instance.total,
      'done': ?instance.done,
      'percent': ?instance.percent,
      'ratePerMinute': ?instance.ratePerMinute,
      'etaSeconds': ?instance.etaSeconds,
      'startedAt': ?instance.startedAt?.toIso8601String(),
    };

const _$SearchReindexStateStateEnumEnumMap = {
  SearchReindexStateStateEnum.idle: 'idle',
  SearchReindexStateStateEnum.backfilling: 'backfilling',
  SearchReindexStateStateEnum.indexing: 'indexing',
  SearchReindexStateStateEnum.swapping: 'swapping',
};
