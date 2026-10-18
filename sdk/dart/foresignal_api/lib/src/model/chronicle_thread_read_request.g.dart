// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_thread_read_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleThreadReadRequestCWProxy {
  ChronicleThreadReadRequest upToId(int upToId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleThreadReadRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleThreadReadRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleThreadReadRequest call({int upToId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleThreadReadRequest.copyWith(...)` or call `instanceOfChronicleThreadReadRequest.copyWith.fieldName(value)` for a single field.
class _$ChronicleThreadReadRequestCWProxyImpl
    implements _$ChronicleThreadReadRequestCWProxy {
  const _$ChronicleThreadReadRequestCWProxyImpl(this._value);

  final ChronicleThreadReadRequest _value;

  @override
  ChronicleThreadReadRequest upToId(int upToId) => call(upToId: upToId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleThreadReadRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleThreadReadRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleThreadReadRequest call({
    Object? upToId = const $CopyWithPlaceholder(),
  }) {
    return ChronicleThreadReadRequest(
      upToId: upToId == const $CopyWithPlaceholder() || upToId == null
          ? _value.upToId
          // ignore: cast_nullable_to_non_nullable
          : upToId as int,
    );
  }
}

extension $ChronicleThreadReadRequestCopyWith on ChronicleThreadReadRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleThreadReadRequest.copyWith(...)` or `instanceOfChronicleThreadReadRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleThreadReadRequestCWProxy get copyWith =>
      _$ChronicleThreadReadRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleThreadReadRequest _$ChronicleThreadReadRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleThreadReadRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['upToId']);
  final val = ChronicleThreadReadRequest(
    upToId: $checkedConvert('upToId', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$ChronicleThreadReadRequestToJson(
  ChronicleThreadReadRequest instance,
) => <String, dynamic>{'upToId': instance.upToId};
