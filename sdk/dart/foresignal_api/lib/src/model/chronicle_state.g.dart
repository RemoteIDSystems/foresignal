// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_state.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleStateCWProxy {
  ChronicleState chronicleId(String chronicleId);

  ChronicleState at(DateTime at);

  ChronicleState layers(List<ChronicleStateLayersInner> layers);

  ChronicleState caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleState(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleState call({
    String chronicleId,
    DateTime at,
    List<ChronicleStateLayersInner> layers,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleState.copyWith(...)` or call `instanceOfChronicleState.copyWith.fieldName(value)` for a single field.
class _$ChronicleStateCWProxyImpl implements _$ChronicleStateCWProxy {
  const _$ChronicleStateCWProxyImpl(this._value);

  final ChronicleState _value;

  @override
  ChronicleState chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleState at(DateTime at) => call(at: at);

  @override
  ChronicleState layers(List<ChronicleStateLayersInner> layers) =>
      call(layers: layers);

  @override
  ChronicleState caveats(List<Caveat>? caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleState(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleState call({
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return ChronicleState(
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      layers: layers == const $CopyWithPlaceholder() || layers == null
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<ChronicleStateLayersInner>,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $ChronicleStateCopyWith on ChronicleState {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleState.copyWith(...)` or `instanceOfChronicleState.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleStateCWProxy get copyWith => _$ChronicleStateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleState _$ChronicleStateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleState', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['chronicleId', 'at', 'layers']);
      final val = ChronicleState(
        chronicleId: $checkedConvert('chronicleId', (v) => v as String),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        layers: $checkedConvert(
          'layers',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ChronicleStateLayersInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleStateToJson(ChronicleState instance) =>
    <String, dynamic>{
      'chronicleId': instance.chronicleId,
      'at': instance.at.toIso8601String(),
      'layers': instance.layers.map((e) => e.toJson()).toList(),
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
    };
