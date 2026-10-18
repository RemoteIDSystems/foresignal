// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativePageCWProxy {
  NarrativePage items(List<NarrativeSummary> items);

  NarrativePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativePage(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativePage call({List<NarrativeSummary> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativePage.copyWith(...)` or call `instanceOfNarrativePage.copyWith.fieldName(value)` for a single field.
class _$NarrativePageCWProxyImpl implements _$NarrativePageCWProxy {
  const _$NarrativePageCWProxyImpl(this._value);

  final NarrativePage _value;

  @override
  NarrativePage items(List<NarrativeSummary> items) => call(items: items);

  @override
  NarrativePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return NarrativePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<NarrativeSummary>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $NarrativePageCopyWith on NarrativePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativePage.copyWith(...)` or `instanceOfNarrativePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativePageCWProxy get copyWith => _$NarrativePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativePage _$NarrativePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NarrativePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = NarrativePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => NarrativeSummary.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NarrativePageToJson(NarrativePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
