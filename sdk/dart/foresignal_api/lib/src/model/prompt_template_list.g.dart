// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'prompt_template_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PromptTemplateListCWProxy {
  PromptTemplateList items(List<PromptTemplateSummary> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PromptTemplateList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PromptTemplateList(...).copyWith(id: 12, name: "My name")
  /// ```
  PromptTemplateList call({List<PromptTemplateSummary> items});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPromptTemplateList.copyWith(...)` or call `instanceOfPromptTemplateList.copyWith.fieldName(value)` for a single field.
class _$PromptTemplateListCWProxyImpl implements _$PromptTemplateListCWProxy {
  const _$PromptTemplateListCWProxyImpl(this._value);

  final PromptTemplateList _value;

  @override
  PromptTemplateList items(List<PromptTemplateSummary> items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PromptTemplateList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PromptTemplateList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PromptTemplateList call({Object? items = const $CopyWithPlaceholder()}) {
    return PromptTemplateList(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<PromptTemplateSummary>,
    );
  }
}

extension $PromptTemplateListCopyWith on PromptTemplateList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPromptTemplateList.copyWith(...)` or `instanceOfPromptTemplateList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PromptTemplateListCWProxy get copyWith =>
      _$PromptTemplateListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PromptTemplateList _$PromptTemplateListFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PromptTemplateList', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PromptTemplateList(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    PromptTemplateSummary.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PromptTemplateListToJson(PromptTemplateList instance) =>
    <String, dynamic>{'items': instance.items.map((e) => e.toJson()).toList()};
