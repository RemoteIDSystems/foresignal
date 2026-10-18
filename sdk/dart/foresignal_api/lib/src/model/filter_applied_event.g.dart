// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'filter_applied_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FilterAppliedEventCWProxy {
  FilterAppliedEvent atSeq(int atSeq);

  FilterAppliedEvent filterVersion(int filterVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FilterAppliedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FilterAppliedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  FilterAppliedEvent call({int atSeq, int filterVersion});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFilterAppliedEvent.copyWith(...)` or call `instanceOfFilterAppliedEvent.copyWith.fieldName(value)` for a single field.
class _$FilterAppliedEventCWProxyImpl implements _$FilterAppliedEventCWProxy {
  const _$FilterAppliedEventCWProxyImpl(this._value);

  final FilterAppliedEvent _value;

  @override
  FilterAppliedEvent atSeq(int atSeq) => call(atSeq: atSeq);

  @override
  FilterAppliedEvent filterVersion(int filterVersion) =>
      call(filterVersion: filterVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FilterAppliedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FilterAppliedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FilterAppliedEvent call({
    Object? atSeq = const $CopyWithPlaceholder(),
    Object? filterVersion = const $CopyWithPlaceholder(),
  }) {
    return FilterAppliedEvent(
      atSeq: atSeq == const $CopyWithPlaceholder() || atSeq == null
          ? _value.atSeq
          // ignore: cast_nullable_to_non_nullable
          : atSeq as int,
      filterVersion:
          filterVersion == const $CopyWithPlaceholder() || filterVersion == null
          ? _value.filterVersion
          // ignore: cast_nullable_to_non_nullable
          : filterVersion as int,
    );
  }
}

extension $FilterAppliedEventCopyWith on FilterAppliedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFilterAppliedEvent.copyWith(...)` or `instanceOfFilterAppliedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FilterAppliedEventCWProxy get copyWith =>
      _$FilterAppliedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FilterAppliedEvent _$FilterAppliedEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FilterAppliedEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['atSeq', 'filterVersion']);
      final val = FilterAppliedEvent(
        atSeq: $checkedConvert('atSeq', (v) => (v as num).toInt()),
        filterVersion: $checkedConvert(
          'filterVersion',
          (v) => (v as num).toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$FilterAppliedEventToJson(FilterAppliedEvent instance) =>
    <String, dynamic>{
      'atSeq': instance.atSeq,
      'filterVersion': instance.filterVersion,
    };
