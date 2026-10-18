// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_cycle_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisCyclePageCWProxy {
  AnalysisCyclePage items(List<AnalysisCycle> items);

  AnalysisCyclePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCyclePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCyclePage(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisCyclePage call({List<AnalysisCycle> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisCyclePage.copyWith(...)` or call `instanceOfAnalysisCyclePage.copyWith.fieldName(value)` for a single field.
class _$AnalysisCyclePageCWProxyImpl implements _$AnalysisCyclePageCWProxy {
  const _$AnalysisCyclePageCWProxyImpl(this._value);

  final AnalysisCyclePage _value;

  @override
  AnalysisCyclePage items(List<AnalysisCycle> items) => call(items: items);

  @override
  AnalysisCyclePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCyclePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCyclePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisCyclePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AnalysisCyclePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AnalysisCycle>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AnalysisCyclePageCopyWith on AnalysisCyclePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisCyclePage.copyWith(...)` or `instanceOfAnalysisCyclePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisCyclePageCWProxy get copyWith =>
      _$AnalysisCyclePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisCyclePage _$AnalysisCyclePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalysisCyclePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AnalysisCyclePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AnalysisCycle.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnalysisCyclePageToJson(AnalysisCyclePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
