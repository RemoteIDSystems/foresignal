// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'overland_ack.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OverlandAckCWProxy {
  OverlandAck result(OverlandAckResultEnum result);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OverlandAck(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OverlandAck(...).copyWith(id: 12, name: "My name")
  /// ```
  OverlandAck call({OverlandAckResultEnum result});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOverlandAck.copyWith(...)` or call `instanceOfOverlandAck.copyWith.fieldName(value)` for a single field.
class _$OverlandAckCWProxyImpl implements _$OverlandAckCWProxy {
  const _$OverlandAckCWProxyImpl(this._value);

  final OverlandAck _value;

  @override
  OverlandAck result(OverlandAckResultEnum result) => call(result: result);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OverlandAck(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OverlandAck(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OverlandAck call({Object? result = const $CopyWithPlaceholder()}) {
    return OverlandAck(
      result: result == const $CopyWithPlaceholder() || result == null
          ? _value.result
          // ignore: cast_nullable_to_non_nullable
          : result as OverlandAckResultEnum,
    );
  }
}

extension $OverlandAckCopyWith on OverlandAck {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOverlandAck.copyWith(...)` or `instanceOfOverlandAck.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OverlandAckCWProxy get copyWith => _$OverlandAckCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OverlandAck _$OverlandAckFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OverlandAck', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['result']);
      final val = OverlandAck(
        result: $checkedConvert(
          'result',
          (v) => $enumDecode(_$OverlandAckResultEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OverlandAckToJson(OverlandAck instance) =>
    <String, dynamic>{
      'result': _$OverlandAckResultEnumEnumMap[instance.result]!,
    };

const _$OverlandAckResultEnumEnumMap = {OverlandAckResultEnum.ok: 'ok'};
