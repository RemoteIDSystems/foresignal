// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_suggest_request_hints.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingSuggestRequestHintsCWProxy {
  MappingSuggestRequestHints kind(String? kind);

  MappingSuggestRequestHints description(String? description);

  MappingSuggestRequestHints sourceId(String? sourceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingSuggestRequestHints(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingSuggestRequestHints(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingSuggestRequestHints call({
    String? kind,
    String? description,
    String? sourceId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingSuggestRequestHints.copyWith(...)` or call `instanceOfMappingSuggestRequestHints.copyWith.fieldName(value)` for a single field.
class _$MappingSuggestRequestHintsCWProxyImpl
    implements _$MappingSuggestRequestHintsCWProxy {
  const _$MappingSuggestRequestHintsCWProxyImpl(this._value);

  final MappingSuggestRequestHints _value;

  @override
  MappingSuggestRequestHints kind(String? kind) => call(kind: kind);

  @override
  MappingSuggestRequestHints description(String? description) =>
      call(description: description);

  @override
  MappingSuggestRequestHints sourceId(String? sourceId) =>
      call(sourceId: sourceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingSuggestRequestHints(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingSuggestRequestHints(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingSuggestRequestHints call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
  }) {
    return MappingSuggestRequestHints(
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
    );
  }
}

extension $MappingSuggestRequestHintsCopyWith on MappingSuggestRequestHints {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingSuggestRequestHints.copyWith(...)` or `instanceOfMappingSuggestRequestHints.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingSuggestRequestHintsCWProxy get copyWith =>
      _$MappingSuggestRequestHintsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingSuggestRequestHints _$MappingSuggestRequestHintsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingSuggestRequestHints', json, ($checkedConvert) {
  final val = MappingSuggestRequestHints(
    kind: $checkedConvert('kind', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String?),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$MappingSuggestRequestHintsToJson(
  MappingSuggestRequestHints instance,
) => <String, dynamic>{
  'kind': ?instance.kind,
  'description': ?instance.description,
  'sourceId': ?instance.sourceId,
};
