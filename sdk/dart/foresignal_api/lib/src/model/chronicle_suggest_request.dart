//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_area.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_suggest_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSuggestRequest {
  /// Returns a new [ChronicleSuggestRequest] instance.
  ChronicleSuggestRequest({

     this.situationId,

     this.area,

     this.topic,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final ChronicleArea? area;



      /// 117 attention topic id or free keywords
  @JsonKey(
    
    name: r'topic',
    required: false,
    includeIfNull: false,
  )


  final String? topic;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSuggestRequest &&
      other.situationId == situationId &&
      other.area == area &&
      other.topic == topic;

    @override
    int get hashCode =>
        (situationId == null ? 0 : situationId.hashCode) +
        (area == null ? 0 : area.hashCode) +
        (topic == null ? 0 : topic.hashCode);

  factory ChronicleSuggestRequest.fromJson(Map<String, dynamic> json) => _$ChronicleSuggestRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSuggestRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

