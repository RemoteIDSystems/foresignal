// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_merge_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventMergeInputCWProxy {
  IntelEventMergeInput intoId(String intoId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventMergeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventMergeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventMergeInput call({String intoId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventMergeInput.copyWith(...)` or call `instanceOfIntelEventMergeInput.copyWith.fieldName(value)` for a single field.
class _$IntelEventMergeInputCWProxyImpl
    implements _$IntelEventMergeInputCWProxy {
  const _$IntelEventMergeInputCWProxyImpl(this._value);

  final IntelEventMergeInput _value;

  @override
  IntelEventMergeInput intoId(String intoId) => call(intoId: intoId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventMergeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventMergeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventMergeInput call({Object? intoId = const $CopyWithPlaceholder()}) {
    return IntelEventMergeInput(
      intoId: intoId == const $CopyWithPlaceholder() || intoId == null
          ? _value.intoId
          // ignore: cast_nullable_to_non_nullable
          : intoId as String,
    );
  }
}

extension $IntelEventMergeInputCopyWith on IntelEventMergeInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventMergeInput.copyWith(...)` or `instanceOfIntelEventMergeInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventMergeInputCWProxy get copyWith =>
      _$IntelEventMergeInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventMergeInput _$IntelEventMergeInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelEventMergeInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['intoId']);
  final val = IntelEventMergeInput(
    intoId: $checkedConvert('intoId', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$IntelEventMergeInputToJson(
  IntelEventMergeInput instance,
) => <String, dynamic>{'intoId': instance.intoId};
