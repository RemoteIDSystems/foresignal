//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_external_stream_selector.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleExternalStreamSelector {
  /// Returns a new [ChronicleExternalStreamSelector] instance.
  ChronicleExternalStreamSelector({

    required  this.type,

    required  this.sourceId,

    required  this.streamId,

     this.filter,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleExternalStreamSelectorTypeEnum type;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'streamId',
    required: true,
    includeIfNull: false,
  )


  final String streamId;



  @JsonKey(
    
    name: r'filter',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? filter;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleExternalStreamSelector &&
      other.type == type &&
      other.sourceId == sourceId &&
      other.streamId == streamId &&
      other.filter == filter;

    @override
    int get hashCode =>
        type.hashCode +
        sourceId.hashCode +
        streamId.hashCode +
        (filter == null ? 0 : filter.hashCode);

  factory ChronicleExternalStreamSelector.fromJson(Map<String, dynamic> json) => _$ChronicleExternalStreamSelectorFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleExternalStreamSelectorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleExternalStreamSelectorTypeEnum {
@JsonValue(r'external-stream')
externalStream(r'external-stream');

const ChronicleExternalStreamSelectorTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


